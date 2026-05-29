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

import { svg } from 'lit';
import { getNodeColor, lightenColor } from './colors.js';
import { hasScope, expandBoundsForBox, V_GAP, SCOPE_BOX_PAD, PADDING } from './layout-engine.js';

const ARC = 7;
const ARROW_SIZE = 6;
const BADGE_R = 9;

const BRANCH_CHILD_SET = new Set(['when', 'otherwise', 'doCatch', 'doFinally', 'onFallback']);

function getTopY(node, nodeWidth) {
  return node.treeNode != null && hasScope(node.treeNode)
    ? node.y - SCOPE_BOX_PAD : node.y;
}

function renderScopeBox(scopeNode, nodeWidth, theme) {
  const tn = scopeNode.treeNode;
  const bounds = {
    minX: scopeNode.x,
    minY: scopeNode.y,
    maxX: scopeNode.x + nodeWidth,
    maxY: scopeNode.y + scopeNode.height,
  };
  for (const child of tn.children) {
    expandBoundsForBox(child, bounds, nodeWidth);
  }

  const boxX = bounds.minX - SCOPE_BOX_PAD;
  const boxY = bounds.minY - SCOPE_BOX_PAD;
  const boxW = bounds.maxX - bounds.minX + 2 * SCOPE_BOX_PAD;
  const boxH = bounds.maxY - bounds.minY + 2 * SCOPE_BOX_PAD;

  return svg`
    <rect x="${boxX}" y="${boxY}" width="${boxW}" height="${boxH}"
          rx="${ARC}" fill="none"
          stroke="${theme.arrow}" stroke-opacity="0.55"
          stroke-dasharray="10 5" stroke-width="1.5" />
  `;
}

function renderArrow(from, to, nodeWidth, theme, metrics) {
  const stat = metrics ? to.treeNode.info.stat : null;
  let total = 0, failed = 0;
  if (metrics && BRANCH_CHILD_SET.has(to.type) && to.treeNode.children.length > 0) {
    const childStat = to.treeNode.children[0].info.stat;
    total = childStat ? childStat.exchangesTotal : 0;
    failed = childStat ? childStat.exchangesFailed : 0;
  } else {
    total = stat ? stat.exchangesTotal : 0;
    failed = stat ? stat.exchangesFailed : 0;
  }
  const ok = total - failed;

  const fromCx = from.x + nodeWidth / 2;
  const fromBy = from.y + from.height;
  const toCx = to.x + nodeWidth / 2;
  const toTy = getTopY(to, nodeWidth);

  const dashed = metrics && total === 0;
  const dashAttr = dashed ? '10 5' : 'none';

  let pathD;
  if (fromCx === toCx) {
    pathD = `M ${fromCx} ${fromBy} L ${toCx} ${toTy - ARROW_SIZE / 2}`;
  } else {
    const midY = fromBy + V_GAP / 2;
    pathD = `M ${fromCx} ${fromBy} L ${fromCx} ${midY} L ${toCx} ${midY} L ${toCx} ${toTy - ARROW_SIZE / 2}`;
  }

  const arrowHead = `${toCx - ARROW_SIZE},${toTy - ARROW_SIZE} ${toCx},${toTy} ${toCx + ARROW_SIZE},${toTy - ARROW_SIZE}`;

  return svg`
    <g class="arrow">
      <path d="${pathD}" fill="none" stroke="${theme.arrow}" stroke-width="1.5"
            stroke-dasharray="${dashAttr}" />
      <polygon points="${arrowHead}" fill="${theme.arrow}" />
      ${metrics && ok > 0 ? svg`
        <text x="${toCx + 8}" y="${toTy - 8}"
              fill="${theme.counterOk}" font-size="10" font-family="sans-serif">${ok}</text>
      ` : ''}
      ${metrics && failed > 0 ? svg`
        <text x="${toCx - 8}" y="${toTy - 8}" text-anchor="end"
              fill="${theme.counterFail}" font-size="10" font-family="sans-serif">${failed}</text>
      ` : ''}
    </g>
  `;
}

function renderArrowFromMerge(to, nodeWidth, theme, metrics) {
  const stat = to.treeNode.info.stat;
  const total = stat ? stat.exchangesTotal : 0;
  const failed = stat ? stat.exchangesFailed : 0;
  const ok = total - failed;

  const toCx = to.x + nodeWidth / 2;
  const toTy = getTopY(to, nodeWidth);
  const mergeCx = to.mergeCx;
  const mergeY = to.mergeY;

  const dashed = metrics && total === 0;
  const dashAttr = dashed ? '10 5' : 'none';

  let pathD;
  if (mergeCx === toCx) {
    pathD = `M ${mergeCx} ${mergeY} L ${toCx} ${toTy - ARROW_SIZE / 2}`;
  } else {
    const midY = mergeY + (toTy - mergeY) / 2;
    pathD = `M ${mergeCx} ${mergeY} L ${mergeCx} ${midY} L ${toCx} ${midY} L ${toCx} ${toTy - ARROW_SIZE / 2}`;
  }

  const arrowHead = `${toCx - ARROW_SIZE},${toTy - ARROW_SIZE} ${toCx},${toTy} ${toCx + ARROW_SIZE},${toTy - ARROW_SIZE}`;

  return svg`
    <g class="arrow merge-arrow">
      <path d="${pathD}" fill="none" stroke="${theme.arrow}" stroke-width="1.5"
            stroke-dasharray="${dashAttr}" />
      <polygon points="${arrowHead}" fill="${theme.arrow}" />
      ${metrics && ok > 0 ? svg`
        <text x="${toCx + 8}" y="${toTy - 8}"
              fill="${theme.counterOk}" font-size="10" font-family="sans-serif">${ok}</text>
      ` : ''}
      ${metrics && failed > 0 ? svg`
        <text x="${toCx - 8}" y="${toTy - 8}" text-anchor="end"
              fill="${theme.counterFail}" font-size="10" font-family="sans-serif">${failed}</text>
      ` : ''}
    </g>
  `;
}

function renderNode(node, nodeWidth, fontSize, theme, options = {}) {
  const fillColor = getNodeColor(node.type, theme);
  const borderColor = lightenColor(fillColor);
  const lines = node.wrappedLines;
  const isSelected = options.selectable && options.selectedNodeId === node.id;

  const lineHeight = fontSize * 1.4;
  const totalTextHeight = lines.length * lineHeight;
  const startY = node.y + (node.height - totalTextHeight) / 2 + fontSize;

  const stat = node.treeNode ? node.treeNode.info.stat : null;
  const showBadge = options.nodeCounters && stat && stat.exchangesTotal > 0;

  return svg`
    <g class="node ${isSelected ? 'node-selected' : ''}"
       data-node-id="${node.id}" data-node-type="${node.type}">
      <rect x="${node.x}" y="${node.y}" width="${nodeWidth}" height="${node.height}"
            rx="${ARC}" fill="${fillColor}"
            stroke="${isSelected ? theme.selectedStroke : borderColor}"
            stroke-width="${isSelected ? 2.5 : 1}" />
      ${lines.map((line, i) => svg`
        <text x="${node.x + nodeWidth / 2}" y="${startY + i * lineHeight}"
              text-anchor="middle" fill="${theme.text}"
              font-size="${fontSize}" font-family="sans-serif">${line}</text>
      `)}
      ${showBadge ? svg`
        <circle cx="${node.x + nodeWidth - 4}" cy="${node.y + 4}"
                r="${BADGE_R}" fill="${theme.badgeBg}" />
        <text x="${node.x + nodeWidth - 4}" y="${node.y + 4 + 3.5}"
              text-anchor="middle" fill="${theme.badgeText}"
              font-size="9" font-weight="bold" font-family="sans-serif">${stat.exchangesTotal}</text>
      ` : ''}
      ${options.selectable || options.tooltips ? svg`
        <rect class="node-hitarea" x="${node.x}" y="${node.y}"
              width="${nodeWidth}" height="${node.height}"
              fill="transparent" style="cursor: pointer" />
      ` : ''}
    </g>
  `;
}

function renderTooltip(node, nodeWidth, fontSize, theme) {
  const stat = node.treeNode ? node.treeNode.info.stat : null;
  const tipW = 180;
  const tipH = stat ? 120 : 40;
  const tipX = node.x + (nodeWidth - tipW) / 2;
  const tipY = node.y - tipH - 8;

  return svg`
    <foreignObject x="${tipX}" y="${tipY}" width="${tipW}" height="${tipH}"
                   class="tooltip-fo" style="pointer-events: none; overflow: visible;">
      <div xmlns="http://www.w3.org/1999/xhtml"
           style="background: ${theme.tooltipBg}; color: ${theme.tooltipText};
                  border: 1px solid ${theme.tooltipBorder}; border-radius: 6px;
                  padding: 6px 8px; font-size: 10px; font-family: sans-serif;
                  box-shadow: 0 2px 8px rgba(0,0,0,0.3);">
        <div style="font-weight: bold; margin-bottom: 4px;">
          ${node.type}${node.id ? ` (${node.id})` : ''}
        </div>
        ${stat ? svg`
          <table xmlns="http://www.w3.org/1999/xhtml"
                 style="width: 100%; border-collapse: collapse; font-size: 9px;">
            <tr><td>Total</td><td style="text-align:right">${stat.exchangesTotal}</td>
                <td>Failed</td><td style="text-align:right; color:${theme.counterFail}">${stat.exchangesFailed}</td></tr>
            <tr><td>Inflight</td><td style="text-align:right">${stat.exchangesInflight || 0}</td>
                <td></td><td></td></tr>
            <tr><td>Mean</td><td style="text-align:right">${stat.meanProcessingTime}ms</td>
                <td>Last</td><td style="text-align:right">${stat.lastProcessingTime || '-'}ms</td></tr>
            <tr><td>Min</td><td style="text-align:right">${stat.minProcessingTime}ms</td>
                <td>Max</td><td style="text-align:right">${stat.maxProcessingTime}ms</td></tr>
          </table>
        ` : ''}
      </div>
    </foreignObject>
  `;
}

export function renderRoute(lr, nodeWidth, fontSize, theme, metrics, options = {}) {
  const scopeBoxes = [];
  const arrows = [];
  const nodes = [];

  let label = lr.routeId;
  if (lr.source) label += ` (${lr.source})`;

  for (const ln of lr.nodes) {
    if (ln.treeNode != null && hasScope(ln.treeNode)) {
      scopeBoxes.push(renderScopeBox(ln, nodeWidth, theme));
    }
  }

  for (const ln of lr.nodes) {
    if (ln.parentNode != null) {
      if (ln.connectFromMerge) {
        arrows.push(renderArrowFromMerge(ln, nodeWidth, theme, metrics));
      } else {
        arrows.push(renderArrow(ln.parentNode, ln, nodeWidth, theme, metrics));
      }
    }
  }

  for (const ln of lr.nodes) {
    nodes.push(renderNode(ln, nodeWidth, fontSize, theme, options));
  }

  const tooltip = options.tooltips && options.hoveredNodeId
    ? lr.nodes.find(n => n.id === options.hoveredNodeId)
    : null;

  return svg`
    <g class="route" data-route-id="${lr.routeId}">
      <text x="${PADDING}" y="${lr.labelY + 14}"
            fill="${theme.label}" font-size="${fontSize + 1}"
            font-weight="bold" font-family="sans-serif">${label}</text>
      ${scopeBoxes}
      ${arrows}
      ${nodes}
      ${tooltip ? renderTooltip(tooltip, nodeWidth, fontSize, theme) : ''}
    </g>
  `;
}

export function renderDiagram(layoutRoutes, totalHeight, nodeWidth, fontSize, theme, metrics, options = {}) {
  const imgWidth = Math.max(...layoutRoutes.map(lr => lr.maxX), 400) + PADDING;
  const imgHeight = totalHeight + PADDING;

  let viewBox;
  if (options.fitView) {
    const pad = Math.max(imgWidth, imgHeight) * 0.05;
    viewBox = `${-pad} ${-pad} ${imgWidth + 2 * pad} ${imgHeight + 2 * pad}`;
  } else {
    viewBox = `0 0 ${imgWidth} ${imgHeight}`;
  }

  const content = layoutRoutes.map(lr => renderRoute(lr, nodeWidth, fontSize, theme, metrics, options));

  if (options.panZoom) {
    const tx = options.viewTransform || { x: 0, y: 0, scale: 1 };
    return svg`
      <svg xmlns="http://www.w3.org/2000/svg"
           viewBox="${viewBox}"
           width="100%" height="100%"
           style="max-width: ${options.fitView ? '100%' : imgWidth + 'px'}">
        <g class="pan-zoom-layer"
           transform="translate(${tx.x}, ${tx.y}) scale(${tx.scale})">
          ${content}
        </g>
      </svg>
    `;
  }

  return svg`
    <svg xmlns="http://www.w3.org/2000/svg"
         viewBox="${viewBox}"
         width="100%" height="100%"
         style="max-width: ${options.fitView ? '100%' : imgWidth + 'px'}">
      ${content}
    </svg>
  `;
}
