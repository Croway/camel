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

import { LitElement, html, css } from 'lit';
import { parseRoutes, layoutRoute, V_GAP, PADDING, DEFAULT_BOX_WIDTH, DEFAULT_FONT_SIZE } from './layout-engine.js';
import { renderDiagram } from './svg-renderer.js';
import { DARK_THEME, LIGHT_THEME } from './colors.js';
import { fetchRouteStructure, PollingController } from './data-fetcher.js';

export class CamelRouteDiagram extends LitElement {

  static properties = {
    src: { type: String },
    refresh: { type: Number },
    filter: { type: String },
    nodeWidth: { type: Number, attribute: 'node-width' },
    fontSize: { type: Number, attribute: 'font-size' },
    nodeLabel: { type: String, attribute: 'node-label' },
    metric: { type: Boolean },
    tooltips: { type: Boolean },
    nodeCounters: { type: Boolean, attribute: 'node-counters' },
    selectable: { type: Boolean },
    panZoom: { type: Boolean, attribute: 'pan-zoom' },
    fitView: { type: Boolean, attribute: 'fit-view' },
    _routes: { state: true },
    _error: { state: true },
    _loading: { state: true },
    _selectedNodeId: { state: true },
    _hoveredNodeId: { state: true },
    _viewTransform: { state: true },
  };

  static styles = css`
    :host {
      display: block;
      font-family: sans-serif;
      --diagram-bg: transparent;
    }

    .container {
      overflow: auto;
      background: var(--diagram-bg);
    }

    .container.pan-zoom-active {
      overflow: hidden;
      cursor: grab;
    }

    .container.pan-zoom-active.panning {
      cursor: grabbing;
    }

    .loading {
      padding: 16px;
      color: var(--diagram-text, #888);
      font-style: italic;
    }

    .error {
      padding: 16px;
      color: var(--diagram-error, #e53935);
    }

    .empty {
      padding: 16px;
      color: var(--diagram-text, #888);
    }

    svg text {
      user-select: none;
    }

    .node {
      cursor: default;
    }

    .node:hover rect:first-child {
      filter: brightness(1.2);
    }

    .node-selected rect:first-child {
      filter: brightness(1.1);
    }
  `;

  constructor() {
    super();
    this.src = '';
    this.refresh = 0;
    this.filter = '';
    this.nodeWidth = DEFAULT_BOX_WIDTH;
    this.fontSize = DEFAULT_FONT_SIZE;
    this.nodeLabel = 'code';
    this.metric = true;
    this.tooltips = false;
    this.nodeCounters = false;
    this.selectable = false;
    this.panZoom = false;
    this.fitView = false;
    this._routes = null;
    this._error = null;
    this._loading = false;
    this._selectedNodeId = null;
    this._hoveredNodeId = null;
    this._viewTransform = { x: 0, y: 0, scale: 1 };
    this._panning = false;
    this._panStart = null;
    this._polling = new PollingController(this, () => this._fetchData());
  }

  connectedCallback() {
    super.connectedCallback();
    if (this.src) {
      this._fetchData();
    }
    if (this.refresh > 0) {
      this._polling.start(this.refresh);
    }
  }

  disconnectedCallback() {
    super.disconnectedCallback();
    this._polling.stop();
  }

  updated(changed) {
    if (changed.has('src') && this.src && !this._loading) {
      this._fetchData();
    }
    if (changed.has('refresh')) {
      this._polling.stop();
      if (this.refresh > 0) {
        this._polling.start(this.refresh);
      }
    }
    if (changed.has('filter') && this.src && !this._loading) {
      this._fetchData();
    }
  }

  async _fetchData() {
    if (!this.src) return;

    this._loading = true;
    try {
      const json = await fetchRouteStructure(this.src, this.metric, this.filter);
      this._routes = parseRoutes(json);
      this._error = null;
    } catch (e) {
      this._error = e.message;
    } finally {
      this._loading = false;
    }
  }

  _getTheme() {
    if (window.matchMedia && window.matchMedia('(prefers-color-scheme: light)').matches) {
      return LIGHT_THEME;
    }
    return DARK_THEME;
  }

  _findNodeById(layoutRoutes, nodeId) {
    for (const lr of layoutRoutes) {
      const found = lr.nodes.find(n => n.id === nodeId);
      if (found) return found;
    }
    return null;
  }

  _handleSvgClick(e) {
    const nodeEl = e.target.closest('.node');
    if (nodeEl && this.selectable) {
      const nodeId = nodeEl.dataset.nodeId;
      this._selectedNodeId = nodeId;
      const nodeType = nodeEl.dataset.nodeType;
      this.dispatchEvent(new CustomEvent('node-selected', {
        detail: { nodeId, type: nodeType },
        bubbles: true,
        composed: true,
      }));
    } else if (this.selectable) {
      this._selectedNodeId = null;
    }
  }

  _handleNodeMouseEnter(e) {
    if (!this.tooltips) return;
    const nodeEl = e.target.closest('.node');
    if (nodeEl) {
      this._hoveredNodeId = nodeEl.dataset.nodeId;
    }
  }

  _handleNodeMouseLeave(e) {
    if (!this.tooltips) return;
    this._hoveredNodeId = null;
  }

  _handleWheel(e) {
    if (!this.panZoom) return;
    e.preventDefault();
    const delta = e.deltaY > 0 ? 0.9 : 1.1;
    const t = this._viewTransform;
    const newScale = Math.max(0.2, Math.min(5, t.scale * delta));
    this._viewTransform = { ...t, scale: newScale };
  }

  _handlePointerDown(e) {
    if (!this.panZoom) return;
    this._panning = true;
    this._panStart = { x: e.clientX - this._viewTransform.x, y: e.clientY - this._viewTransform.y };
    const container = this.shadowRoot.querySelector('.container');
    if (container) container.classList.add('panning');
    e.target.setPointerCapture(e.pointerId);
  }

  _handlePointerMove(e) {
    if (!this._panning || !this._panStart) return;
    const t = this._viewTransform;
    this._viewTransform = {
      ...t,
      x: e.clientX - this._panStart.x,
      y: e.clientY - this._panStart.y,
    };
  }

  _handlePointerUp(e) {
    if (!this._panning) return;
    this._panning = false;
    this._panStart = null;
    const container = this.shadowRoot.querySelector('.container');
    if (container) container.classList.remove('panning');
  }

  render() {
    if (this._error) {
      return html`<div class="error">${this._error}</div>`;
    }

    if (!this._routes) {
      return html`<div class="loading">Loading route diagram...</div>`;
    }

    if (this._routes.length === 0) {
      return html`<div class="empty">No routes found.</div>`;
    }

    const theme = this._getTheme();
    const layoutOptions = {
      nodeWidth: this.nodeWidth,
      fontSize: this.fontSize,
      nodeLabel: this.nodeLabel,
    };

    const layoutRoutes = [];
    let currentY = PADDING;
    for (const route of this._routes) {
      const lr = layoutRoute(route, currentY, layoutOptions);
      layoutRoutes.push(lr);
      currentY = lr.maxY + V_GAP;
    }

    const totalHeight = currentY;
    const renderOptions = {
      tooltips: this.tooltips,
      hoveredNodeId: this._hoveredNodeId,
      nodeCounters: this.nodeCounters,
      selectable: this.selectable,
      selectedNodeId: this._selectedNodeId,
      panZoom: this.panZoom,
      viewTransform: this._viewTransform,
      fitView: this.fitView,
    };

    const diagram = renderDiagram(layoutRoutes, totalHeight, this.nodeWidth, this.fontSize, theme, this.metric, renderOptions);

    const containerClass = `container${this.panZoom ? ' pan-zoom-active' : ''}`;

    return html`
      <div class="${containerClass}"
           @click="${this._handleSvgClick}"
           @mouseover="${this._handleNodeMouseEnter}"
           @mouseout="${this._handleNodeMouseLeave}"
           @wheel="${this._handleWheel}"
           @pointerdown="${this._handlePointerDown}"
           @pointermove="${this._handlePointerMove}"
           @pointerup="${this._handlePointerUp}">
        ${diagram}
      </div>
    `;
  }
}

customElements.define('camel-route-diagram', CamelRouteDiagram);
