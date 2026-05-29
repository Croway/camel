/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

export const V_GAP = 40;
export const PADDING = 30;
export const SCOPE_BOX_PAD = 14;
const LABEL_OFFSET = 24;
const MAX_WRAP_LINES = 3;
export const DEFAULT_BOX_WIDTH = 180;
const MIN_BOX_WIDTH = 80;
const DEFAULT_NODE_HEIGHT = 32;
export const DEFAULT_FONT_SIZE = 12;

const BRANCHING_EIPS = new Set([
  'choice', 'multicast', 'doTry', 'loadBalance', 'recipientList', 'circuitBreaker',
]);

const BRANCH_CHILD_TYPES = new Set([
  'when', 'otherwise', 'doCatch', 'doFinally', 'onFallback',
]);

const STRUCTURAL_TYPES = new Set(['route', 'from']);

export function isBranchingEip(type) {
  return type != null && BRANCHING_EIPS.has(type);
}

export function hasScope(node) {
  return node.parent != null
    && node.children.length > 0
    && !BRANCH_CHILD_TYPES.has(node.info.type)
    && !STRUCTURAL_TYPES.has(node.info.type);
}

export function cleanLabel(code) {
  if (code == null) return '';
  return code.replace(/^\./, '');
}

export function resolveLabel(info, mode) {
  const code = cleanLabel(info.code);
  const hasDesc = info.description != null && info.description.trim().length > 0;
  switch (mode) {
    case 'description':
      return [hasDesc ? info.description : code];
    case 'both':
      if (hasDesc && info.description !== code) {
        return [info.description, code];
      }
      return [code];
    default:
      return [code];
  }
}

export function buildTree(nodes) {
  if (!nodes || nodes.length === 0) return null;

  const root = { info: nodes[0], parent: null, children: [], subtreeWidth: 0, layoutNode: null };
  let current = root;

  for (let i = 1; i < nodes.length; i++) {
    const ni = nodes[i];
    const tn = { info: ni, parent: null, children: [], subtreeWidth: 0, layoutNode: null };

    if (ni.level > current.info.level) {
      current.children.push(tn);
      tn.parent = current;
    } else if (ni.level === current.info.level) {
      const parent = current.parent;
      if (parent != null) {
        parent.children.push(tn);
        tn.parent = parent;
      } else {
        root.children.push(tn);
        tn.parent = root;
      }
    } else {
      let ancestor = current.parent;
      while (ancestor != null && ancestor.info.level >= ni.level) {
        ancestor = ancestor.parent;
      }
      if (ancestor != null) {
        ancestor.children.push(tn);
        tn.parent = ancestor;
      } else {
        root.children.push(tn);
        tn.parent = root;
      }
    }
    current = tn;
  }
  return root;
}

function estimateCharWidth(fontSize) {
  return fontSize * 0.6;
}

function wrapLabel(label, nodeWidth, nodeTextPadding, fontSize) {
  const maxTextWidth = nodeWidth - nodeTextPadding;
  const charWidth = estimateCharWidth(fontSize);

  if (label.length * charWidth <= maxTextWidth) {
    return [label];
  }

  const lines = [];
  let remaining = label;

  while (remaining.length > 0 && lines.length < MAX_WRAP_LINES) {
    if (remaining.length * charWidth <= maxTextWidth) {
      lines.push(remaining);
      remaining = '';
      break;
    }

    let breakAt = -1;
    const maxChars = Math.floor(maxTextWidth / charWidth);

    for (let j = 0; j < Math.min(remaining.length, maxChars + 1); j++) {
      const c = remaining[j];
      if (c === ' ' || c === ':' || c === '/' || c === '.' || c === ',' || c === '&' || c === '?') {
        breakAt = j + 1;
      }
    }

    if (breakAt <= 0) {
      breakAt = Math.min(maxChars, remaining.length);
      if (breakAt <= 0) breakAt = 1;
    }

    lines.push(remaining.substring(0, breakAt));
    remaining = remaining.substring(breakAt).trimStart();
  }

  if (remaining.length > 0) {
    const lastIdx = lines.length - 1;
    lines[lastIdx] = lines[lastIdx] + remaining;
  }
  return lines;
}

function computeNodeHeight(lines, baseNodeHeight, fontSize) {
  const lineCount = Math.min(lines.length, MAX_WRAP_LINES);
  if (lineCount <= 1) return baseNodeHeight;
  const lineSpacing = fontSize * 1.4;
  return baseNodeHeight + (lineCount - 1) * lineSpacing;
}

function computeSubtreeWidth(node, nodeWidth, hGap) {
  if (node.children.length === 0) {
    node.subtreeWidth = nodeWidth;
    return node.subtreeWidth;
  }

  if (isBranchingEip(node.info.type)) {
    let totalWidth = 0;
    for (let i = 0; i < node.children.length; i++) {
      if (i > 0) totalWidth += hGap;
      totalWidth += computeSubtreeWidth(node.children[i], nodeWidth, hGap);
    }
    node.subtreeWidth = Math.max(nodeWidth, totalWidth);
  } else {
    let maxChildWidth = nodeWidth;
    for (const child of node.children) {
      maxChildWidth = Math.max(maxChildWidth, computeSubtreeWidth(child, nodeWidth, hGap));
    }
    node.subtreeWidth = maxChildWidth;
  }
  return node.subtreeWidth;
}

export function expandBoundsForBox(node, bounds, nodeWidth) {
  const hasOwnBox = hasScope(node);

  if (hasOwnBox) {
    const inner = {
      minX: node.layoutNode.x,
      minY: node.layoutNode.y,
      maxX: node.layoutNode.x + nodeWidth,
      maxY: node.layoutNode.y + node.layoutNode.height,
    };
    for (const child of node.children) {
      expandBoundsForBox(child, inner, nodeWidth);
    }
    bounds.minX = Math.min(bounds.minX, inner.minX - SCOPE_BOX_PAD);
    bounds.minY = Math.min(bounds.minY, inner.minY - SCOPE_BOX_PAD);
    bounds.maxX = Math.max(bounds.maxX, inner.maxX + SCOPE_BOX_PAD);
    bounds.maxY = Math.max(bounds.maxY, inner.maxY + SCOPE_BOX_PAD);
  } else {
    if (node.layoutNode != null) {
      bounds.minX = Math.min(bounds.minX, node.layoutNode.x);
      bounds.minY = Math.min(bounds.minY, node.layoutNode.y);
      bounds.maxX = Math.max(bounds.maxX, node.layoutNode.x + nodeWidth);
      bounds.maxY = Math.max(bounds.maxY, node.layoutNode.y + node.layoutNode.height);
    }
    for (const child of node.children) {
      expandBoundsForBox(child, bounds, nodeWidth);
    }
  }
}

function findLastLayoutNode(node) {
  if (node.children.length === 0) return node.layoutNode;
  if (isBranchingEip(node.info.type)) return node.layoutNode;
  return findLastLayoutNode(node.children[node.children.length - 1]);
}

function findMaxY(node) {
  let maxY = node.layoutNode != null ? node.layoutNode.y + node.layoutNode.height : 0;
  for (const child of node.children) {
    maxY = Math.max(maxY, findMaxY(child));
  }
  return maxY;
}

function assignPositions(node, x, y, parentWidth, lr, nodeWidth, hGap, nodeTextPadding, fontSize, baseNodeHeight, nodeLabelMode) {
  const availableWidth = Math.max(node.subtreeWidth, parentWidth);
  const nodeX = x + (availableWidth - nodeWidth) / 2;

  const labelParts = resolveLabel(node.info, nodeLabelMode);
  const wrappedLines = labelParts.flatMap(s => wrapLabel(s, nodeWidth, nodeTextPadding, fontSize));
  const height = computeNodeHeight(wrappedLines, baseNodeHeight, fontSize);

  const ln = {
    type: node.info.type,
    id: node.info.id,
    x: nodeX,
    y,
    height,
    wrappedLines,
    parentNode: null,
    treeNode: node,
    connectFromMerge: false,
    mergeY: 0,
    mergeCx: 0,
  };
  node.layoutNode = ln;
  lr.nodes.push(ln);

  if (node.parent != null && node.parent.layoutNode != null) {
    const parentNode = node.parent;
    if (!isBranchingEip(parentNode.info.type)) {
      const myIndex = parentNode.children.indexOf(node);
      if (myIndex > 0) {
        const prevSibling = parentNode.children[myIndex - 1];
        if (hasScope(prevSibling)) {
          ln.connectFromMerge = true;
          const boxBounds = {
            minX: prevSibling.layoutNode.x,
            minY: prevSibling.layoutNode.y,
            maxX: prevSibling.layoutNode.x + nodeWidth,
            maxY: prevSibling.layoutNode.y + prevSibling.layoutNode.height,
          };
          for (const c of prevSibling.children) {
            expandBoundsForBox(c, boxBounds, nodeWidth);
          }
          ln.mergeY = boxBounds.maxY + SCOPE_BOX_PAD;
          ln.mergeCx = prevSibling.layoutNode.x + nodeWidth / 2;
          ln.parentNode = prevSibling.layoutNode;
        } else {
          ln.parentNode = findLastLayoutNode(prevSibling);
        }
      } else {
        ln.parentNode = parentNode.layoutNode;
      }
    } else {
      ln.parentNode = parentNode.layoutNode;
    }
  }

  lr.maxY = Math.max(lr.maxY, y + ln.height);

  if (node.children.length === 0) return;

  const childY = y + ln.height + V_GAP;

  if (isBranchingEip(node.info.type)) {
    let childX = x + (availableWidth - node.subtreeWidth) / 2;
    for (const child of node.children) {
      let adjustedY = childY;
      if (child.children.length > 0 && !BRANCH_CHILD_TYPES.has(child.info.type)) {
        adjustedY += SCOPE_BOX_PAD;
      }
      assignPositions(child, childX, adjustedY, child.subtreeWidth, lr, nodeWidth, hGap, nodeTextPadding, fontSize, baseNodeHeight, nodeLabelMode);
      childX += child.subtreeWidth + hGap;
    }
  } else {
    let curY = childY;
    for (let i = 0; i < node.children.length; i++) {
      const child = node.children[i];
      const adjustedY = hasScope(child) ? curY + SCOPE_BOX_PAD : curY;
      assignPositions(child, x, adjustedY, availableWidth, lr, nodeWidth, hGap, nodeTextPadding, fontSize, baseNodeHeight, nodeLabelMode);
      if (hasScope(child)) {
        const cb = {
          minX: child.layoutNode.x,
          minY: child.layoutNode.y,
          maxX: child.layoutNode.x + nodeWidth,
          maxY: child.layoutNode.y + child.layoutNode.height,
        };
        for (const c of child.children) {
          expandBoundsForBox(c, cb, nodeWidth);
        }
        curY = cb.maxY + SCOPE_BOX_PAD + V_GAP;
      } else {
        curY = findMaxY(child) + V_GAP;
      }
    }
  }
}

export function layoutRoute(route, startY, options = {}) {
  const boxWidth = options.nodeWidth || DEFAULT_BOX_WIDTH;
  const fontSize = options.fontSize || DEFAULT_FONT_SIZE;
  const nodeLabelMode = options.nodeLabel || 'code';

  const clampedWidth = Math.max(boxWidth, MIN_BOX_WIDTH);
  const nodeWidth = clampedWidth;
  const hGap = Math.floor(nodeWidth / 2);
  const nodeTextPadding = Math.max(Math.floor(nodeWidth * 16 / DEFAULT_BOX_WIDTH), 8);
  const baseNodeHeight = Math.max(DEFAULT_NODE_HEIGHT, Math.floor(fontSize * DEFAULT_NODE_HEIGHT / DEFAULT_FONT_SIZE));

  const lr = {
    routeId: route.routeId,
    source: route.source,
    labelY: startY,
    maxX: 0,
    maxY: 0,
    nodes: [],
  };

  const tree = buildTree(route.nodes);
  if (tree == null) {
    lr.maxX = PADDING + nodeWidth;
    lr.maxY = startY + LABEL_OFFSET;
    return lr;
  }

  computeSubtreeWidth(tree, nodeWidth, hGap);
  assignPositions(tree, PADDING, startY + LABEL_OFFSET, tree.subtreeWidth, lr, nodeWidth, hGap, nodeTextPadding, fontSize, baseNodeHeight, nodeLabelMode);

  const extent = {
    minX: tree.layoutNode.x,
    minY: tree.layoutNode.y,
    maxX: tree.layoutNode.x + nodeWidth,
    maxY: tree.layoutNode.y + tree.layoutNode.height,
  };
  for (const child of tree.children) {
    expandBoundsForBox(child, extent, nodeWidth);
  }

  if (extent.minX < PADDING) {
    const shift = PADDING - extent.minX;
    for (const ln of lr.nodes) {
      ln.x += shift;
      if (ln.connectFromMerge) {
        ln.mergeCx += shift;
      }
    }
    extent.maxX += shift;
  }

  lr.maxX = extent.maxX;
  lr.maxY = Math.max(lr.maxY, extent.maxY);

  return lr;
}

export function parseRoutes(json) {
  const routes = [];
  const arr = json.routes;
  if (!arr) return routes;

  for (const o of arr) {
    const route = {
      routeId: o.routeId,
      source: extractSourceName(o.source),
      nodes: [],
    };

    const lines = o.code;
    if (lines) {
      for (const line of lines) {
        const node = {
          type: line.type,
          id: line.id,
          code: line.code,
          description: line.description,
          level: line.level != null ? line.level : 0,
          stat: null,
        };

        if (line.statistics) {
          node.stat = {
            exchangesTotal: line.statistics.exchangesTotal || 0,
            exchangesFailed: line.statistics.exchangesFailed || 0,
            exchangesInflight: line.statistics.exchangesInflight || 0,
            meanProcessingTime: line.statistics.meanProcessingTime || 0,
            maxProcessingTime: line.statistics.maxProcessingTime || 0,
            minProcessingTime: line.statistics.minProcessingTime || 0,
          };
        }
        route.nodes.push(node);
      }
    }
    routes.push(route);
  }
  return routes;
}

function extractSourceName(source) {
  if (!source || source.trim().length === 0) return null;
  source = source.replace(/ /g, '-');
  if (source.startsWith('source:')) {
    source = source.substring(7);
    const dot = source.lastIndexOf('.');
    if (dot >= 0) source = source.substring(dot + 1);
  }
  const colonSlash = source.indexOf('://');
  if (colonSlash >= 0) source = source.substring(colonSlash + 3);
  const colon = source.lastIndexOf(':');
  if (colon >= 0) {
    const afterColon = source.substring(colon + 1);
    if (/^\d+$/.test(afterColon)) {
      source = source.substring(0, colon);
    }
  }
  const slash = source.lastIndexOf('/');
  if (slash >= 0) source = source.substring(slash + 1);
  const backslash = source.lastIndexOf('\\');
  if (backslash >= 0) source = source.substring(backslash + 1);
  return source;
}
