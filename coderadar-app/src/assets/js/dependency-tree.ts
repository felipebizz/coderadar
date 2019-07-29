import * as $ from 'jquery';
import {
  canvasArrow,
  toggle,
  findLastHTMLElement,
  expand,
  checkOnActiveDependency,
  checkHandler, screenshotListener, resizeHandler, timeoutDraw
} from './tree-functions';

let ctx;
let htmlBuffer = [];
let checkDown;
let checkUp;
let headerBackground;
let activeDependency;

$.fn.single_double_click = function(singleClickCallback, doubleClickCallback, timeout?) {
  return this.each(() => {
    let clicks = 0;
    const self = this;
    // if a click occurs
    $(this).click(event => {
      // raise click counter
      clicks++;
      // if this is the first click, start a timer with @timeout millis
      if (clicks === 1) {
        setTimeout(() => {
          // on timer's timeout check if a second click has occurred.
          if (clicks === 1) {
            singleClickCallback.call(self, event);
          } else {
            doubleClickCallback.call(self, event);
          }
          // reset click counter
          clicks = 0;
        }, timeout || 300);
      }
    });
  });
};

export function afterLoad(node) {
  htmlBuffer = [];
  buildRoot(node);
  document.getElementById('3dependencyTree').innerHTML = htmlBuffer.join('');
  checkUp = (document.getElementById('3showUpward') as HTMLInputElement).getAttribute('checked') === 'checked';
  checkDown = (document.getElementById('3showDownward') as HTMLInputElement).getAttribute('checked') === 'checked';
  ctx = (document.getElementById('3canvas') as HTMLCanvasElement).getContext('2d');
  headerBackground = (document.getElementById('3headerBackground') as HTMLElement);

  // add toggle function (click and dblclick)
  const togglers = Array.from(document.getElementsByClassName('clickable'));
  for (const toggler of togglers) {
    $(toggler).single_double_click(() => {
      // set toggler to active dependency
      if (activeDependency === toggler) {
        activeDependency = undefined;
        document.getElementById('3activeDependency').textContent = 'No active dependency chosen.';
      } else {
        activeDependency = toggler;
        // @ts-ignore
        document.getElementById('3activeDependency').textContent = toggler.textContent;
      }
      // clear and draw arrows for active dependency
      ctx.clearRect(0, 0, ctx.canvas.width, ctx.canvas.height);
      loadDependencies(node);
    }, () => {
      if (toggler.nextSibling != null) {
        toggle(toggler, activeDependency, ctx, headerBackground);
        ctx.clearRect(0, 0, ctx.canvas.width, ctx.canvas.height);
        loadDependencies(node);
      }
    });
    // collapse and extend elements
    if (toggler.nextSibling != null) {
      expand(toggler as HTMLElement);
    }
  }

  // show upward listener
  document.getElementById('3showUpward').addEventListener('click', () => {
    checkUp = checkHandler(checkUp, ctx, loadDependencies, node);
  });
  // show upward listener
  document.getElementById('3showDownward').addEventListener('click', () => {
    checkDown = checkHandler(checkDown, ctx, loadDependencies, node);
  });
  // screenshot listener
  screenshotListener('3screenshot');
  // resize listener
  resizeHandler(ctx, headerBackground, loadDependencies, node);
  // set canvas format and draw dependencies
  timeoutDraw(ctx, headerBackground, loadDependencies, node);
}

function loadDependencies(node) {
  if (node.dependencies.length > 0 && node.children.length === 0) {
    listDependencies(node);
  }
  if (node.children.length > 0) {
    for (const child of node.children) {
      loadDependencies(child);
    }
  }
}

function buildRoot(currentNode) {
  htmlBuffer.push(`<table id="3list__root" class="list list__root active">`);
  htmlBuffer.push(`<tr><td class="package package__base"><span id="${currentNode.path}" ` +
    `class="filename-span${currentNode.children.length > 0 && currentNode.packageName !== '' ? ' clickable' : ''}">` +
    `${currentNode.filename}</span>`);
  addTable(currentNode);
  htmlBuffer.push(`</td></tr></table>`);
}

function buildTree(currentNode, span) {
  let level = -1;
  // if currentNode has more than one children, open a hidden tr for displaying child's level
  htmlBuffer.push(currentNode.children.length <= 1 ? '' : '<tr style="display: none">');
  for (const child of currentNode.children) {
    // decide whether child is a package, a class or a class with dependencies
    let classString;
    if (child.children.length > 0) {
      classString = 'package';
    } else if (child.dependencies.length > 0) {
      classString = 'class--dependency';
    } else if (child.children.length === 0 && child.dependencies.length === 0) {
      classString = 'class';
    }

    if (span) {
      htmlBuffer.push(`<span id="${child.path}" class="filename-span` +
        `${(child.children.length > 1 || child.dependencies.length > 0) && child.packageName !== '' ? ' clickable' : ''}">` +
        `${'/' + child.filename}</span>`);

      addTable(child);
    } else {
      htmlBuffer.push(level !== child.level ? '</tr><tr>' : '');
      htmlBuffer.push(`<td class="${classString}"><span id="${child.path}" class="filename-span` +
        `${(child.children.length > 1 || child.dependencies.length > 0) && child.packageName !== '' ? ' clickable' : ''}">` +
        `${child.filename}</span>`
      );
      level = child.level;
      addTable(child);
      htmlBuffer.push('</td>');
    }
  }
  htmlBuffer.push(currentNode.children.length <= 1 ? '' : '</tr>');
}

function addTable(child) {
  if (child.children.length > 1) {
    htmlBuffer.push(`<table class="list${child.packageName !== '' ? ' nested' : ''}">`);
    htmlBuffer.push(buildTree(child, child.children.length === 1));
    htmlBuffer.push('</table>');
  } else {
    htmlBuffer.push(buildTree(child, child.children.length === 1));
  }
}

function listDependencies(currentNode) {
  // ctx.lineWidth = 1;
  // draw arrows to my dependencies and call this function for my dependencies
  if (currentNode.dependencies.length > 0) {
    currentNode.dependencies.forEach(dependency => {
      // find last visible element for dependency as end
      let end = findLastHTMLElement(dependency) as HTMLElement;
      // find last visible element for currentNode as start
      let start = findLastHTMLElement(currentNode) as HTMLElement;

      // if activeDependency is set, draw only activeDependency related dependencies
      if (activeDependency !== undefined) {
        // activeDependency is set and neither start or end
        let draw = checkOnActiveDependency(start, activeDependency);
        if (!draw) {
          draw = checkOnActiveDependency(end, activeDependency);
        }
        if (!draw) {
          return;
        }
      }

      start = start.parentNode as HTMLElement;
      end = end.parentNode as HTMLElement;

      // use jquery for position calculation because plain js position calculation working with offsets returns
      // different values for chrome and firefox
      // (ref: https://stackoverflow.com/questions/1472842/firefox-and-chrome-give-different-values-for-offsettop).
      const startx = $(start).offset().left + start.offsetWidth / 2;
      let starty = $(start).offset().top + start.offsetHeight - $(ctx.canvas).offset().top;
      const endx = $(end).offset().left + end.offsetWidth / 2;
      let endy = $(end).offset().top - $(ctx.canvas).offset().top;

      // ignore all arrows with same start and end node
      if (start !== end) {
        // check if downward dependencies should be shown
        if (checkDown && starty < endy) {
          canvasArrow(ctx, startx, starty, endx, endy, 'black', 1, false);
        }
        // check if upward Dependencies should be shown
        if (checkUp && starty > endy) {
          starty -= start.offsetHeight;
          endy += end.offsetHeight;
          canvasArrow(ctx, startx, starty, endx, endy, 'black', 3, true);
        }
      }
    });
  }
}


