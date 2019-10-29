package io.reflectoring.coderadar.analyzer.levelizedStructureMap.analyzers;

import com.google.re2j.Matcher;
import com.google.re2j.Pattern;
import io.reflectoring.coderadar.vcs.UnableToGetCommitContentException;
import io.reflectoring.coderadar.vcs.port.driven.GetRawCommitContentPort;
import org.apache.axis2.util.JavaUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import io.reflectoring.coderadar.analyzer.levelizedStructureMap.domain.RegexPatternCache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class JavaAnalyzer {

    private RegexPatternCache cache;
    private Pattern importPattern;
    private Pattern fullyClassifiedPattern;
    private Pattern skipPattern;
    private Map<String, Boolean> separators;
    public int count = 0;

    private final GetRawCommitContentPort rawCommitContentPort;

    @Autowired
    public JavaAnalyzer(GetRawCommitContentPort rawCommitContentPort) {
        this.cache = new RegexPatternCache();
        importPattern = cache.getPattern(" (([a-z_$][\\w$]*)\\.)+(([a-zA-Z_$][\\w$]*)|\\*);");
        fullyClassifiedPattern = cache.getPattern("([a-zA-Z_$][\\w$]*\\.)*[a-zA-Z_$][\\w$]*");
        skipPattern = cache.getPattern("[\\w$]+");
        separators = new LinkedHashMap<String, Boolean>();
        {
            // this list is ordered by the estimated order of the separators in a line to minimize skipPattern checks
            separators.put("private ", false);
            separators.put("public ", false);
            separators.put("protected ", false);
            separators.put("(", false);
            separators.put(")", false);
            separators.put("{", false);
            separators.put("=", false);
            separators.put(",", false);
            separators.put("<", false);
            separators.put(">", false);
            separators.put("||", false);
            separators.put("&&", false);
            separators.put("?", false);
            separators.put(":", false);
            separators.put("new ", false);
            separators.put("extends ", false);
            separators.put("implements ", false);
            separators.put("throws ", false);
            separators.put("|", false);
            separators.put("instanceof ", false);
            separators.put("@[a-zA-Z_$][\\w$]*", true);
            separators.put(";", false);
        }
        this.rawCommitContentPort = rawCommitContentPort;
    }

    public String getPackageName(String path, String repository, String commitName) {
        try {
            byte[] bytes = rawCommitContentPort.getCommitContent("", repository, commitName);
            if (bytes != null) {
                String fileContent = clearFileContent(new String(bytes));
                Matcher packageMatcher = cache.getPattern("^(\\s*)package(\\s*)(([A-Za-z_$][\\w$]*)\\.)*([A-Za-z_$][\\w$]*);").matcher(fileContent);
                if (packageMatcher.find()) {
                    Matcher nameMatcher = cache.getPattern(" (([A-Za-z_$][\\w$]*)\\.)*([A-Za-z_$][\\w$]*)").matcher(packageMatcher.group());
                    if (nameMatcher.find()) {
                        return nameMatcher.group().substring(1);
                    }
                } else {
                    return null;
                }
            }
            return "";
        } catch (UnableToGetCommitContentException e) {
            return null;
        }
    }

    public List<String> getValidImportsFromFile(String path, String repository, String commitName) {
        try {
            // TODO that is projectRoot
            byte[] bytes = rawCommitContentPort.getCommitContent("", repository, commitName);
            if (bytes == null) {
                return Collections.emptyList();
            }
            return getValidImports(new String(bytes));
        } catch (UnableToGetCommitContentException e) {
            return Collections.emptyList();
        }
    }

    public List<String> getValidImports(String fileContent) {
        List<String> foundDependencies = new ArrayList<>();
        String[] lines = clearFileContent(fileContent).split("\n");
        for (String line : lines) {
            if (line.matches("^\\s*//.*$") || line.matches("^\\s*/\\*.*$") || line.matches("^\\s*\\*.*$")
                    || line.matches("^.*\".*$") || line.matches("^\\s*package.*$")) {
                continue;
            }
            if (line.contains("import ")) {
                getDependenciesFromImportLine(line).stream().filter(imp -> !foundDependencies.contains(imp)).forEach(foundDependencies::add);
            } else {
                getDependenciesFromLine(line).stream().filter(imp -> !foundDependencies.contains(imp)).forEach(foundDependencies::add);
            }
        }
        return foundDependencies;
    }

    /**
     * Remove multiline comments from fileContent.
     *
     * @param fileContent given fileContent to clean up.
     * @return cleaned fileContent.
     */
    private String clearFileContent(final String fileContent) {
        return cache.getPattern("(\\/\\*(.|[\\r\\n])+?\\*\\/)|(\\/\\/.*[\\r\\n])").matcher(fileContent).replaceAll("");
    }

    /**
     * Get imports from a given line.
     *
     * @param line line to check.
     * @return list of dependency Strings.
     */
    public List<String> getDependenciesFromImportLine(String line) {
        // possibilities for there to be more than one dependency in one line
        //   import org.somepackage.A a; import org.somepackage.B;
        List<String> foundDependencies = new ArrayList<>();
        String lineString = line;
        while (lineString.contains(";")) {
            Matcher importMatcher = importPattern.matcher(lineString);
            if (importMatcher.find()) {
                String importString = importMatcher.group().substring(1, importMatcher.group().length() - 1);
                if (!foundDependencies.contains(importString)) {
                    foundDependencies.add(importString);
                }
            }
            lineString = lineString.substring(lineString.indexOf(";") + 1);
        }
        return foundDependencies;
    }

    /**
     * Get fully classified dependencies and same package dependencies from a line.
     * Possibilities for there to be more than one dependency in one line:
     *   class A extends B,
     *   interface A extends B,
     *   class A implements B, C,
     *   A a = new B(),
     *   A a = B.get(),
     *   private A a; B b;
     *   private A a = B.get(C.create());
     *   private A a = (B) C;
     *   method() throws ExceptionA
     *   catch (ExceptionB | ExceptionC c)
     *   throw new ExceptionD
     *   C test = (D.isEmpty || E.isEmpty) ? A.get() : B.get()
     *
     * @param line line to check.
     * @return list of dependency Strings.
     */
    public List<String> getDependenciesFromLine(String line) {
        List<String> foundDependencies = new ArrayList<>();
        String lineString = line;
        String separator = getFirstSeparator(lineString);
        while (separator != null) {
            Matcher fullyClassifiedMatcher = fullyClassifiedPattern.matcher(lineString);
            if (fullyClassifiedMatcher.find() && !JavaUtils.isJavaKeyword(fullyClassifiedMatcher.group())) {
                if (!foundDependencies.contains(fullyClassifiedMatcher.group())) {
                    foundDependencies.add(fullyClassifiedMatcher.group());
                }
            }
            // split lineString at first found position of found separator
            lineString = lineString.substring(lineString.indexOf(separator) + separator.length());
            Matcher skipMatcher = skipPattern.matcher(lineString);
            if (skipMatcher.find()) {
                separator = getFirstSeparator(lineString);
            } else {
                separator = null;
            }
        }
        return foundDependencies;
    }

    /**
     * Get the first dependency separator in a given String.
     *
     * @param toCheck the String to check.
     * @return the found separator or null of non is found.
     */
    public String getFirstSeparator(String toCheck) {
        int index = toCheck.length();
        String separator = null;
        for (Map.Entry<String, Boolean> entry : separators.entrySet()) {
            if (toCheck.contains("@") && entry.getValue()) {
                // entry is a regex
                Matcher matcher = cache.getPattern(entry.getKey()).matcher(toCheck);
                if (matcher.find()) {
                    String found = matcher.group();
                    if (toCheck.indexOf(found) < index && checkTmpString(found, toCheck)) {
                        separator = matcher.group();
                        index = toCheck.indexOf(separator);
                    }
                }
            } else {
                // entry is not a regex
                if (toCheck.contains(entry.getKey()) && toCheck.indexOf(entry.getKey()) < index && checkTmpString(entry.getKey(), toCheck)) {
                    separator = entry.getKey();
                    index = toCheck.indexOf(separator);
                }
            }
        }
        return separator;
    }

    /**
     * Check if there are potential imports in a given string before the current found first separator.
     *
     * @param border current found first separator.
     * @param string the String to check.
     * @return true if there is a potential import, else false.
     */
    private boolean checkTmpString(String border, String string) {
        count++;
        return skipPattern.matcher(string.substring(0, string.indexOf(border) + border.length())).find();
    }
}
