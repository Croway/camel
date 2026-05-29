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

export const DARK_THEME = {
  bg: '#0d1117',
  text: '#f0f6fc',
  arrow: '#656c76',
  label: '#d1d7e0',
  counterOk: '#2e7d32',
  counterFail: '#ff0000',
  from: '#238636',
  to: '#1f6feb',
  eip: '#8957e5',
  choice: '#d29922',
  default: '#3d444d',
  transform: '#1b7c83',
  processor: '#bf4b8a',
  selectedStroke: '#58a6ff',
  badgeBg: '#d1d7e0',
  badgeText: '#0d1117',
  tooltipBg: '#161b22',
  tooltipText: '#e6edf3',
  tooltipBorder: '#30363d',
};

export const LIGHT_THEME = {
  bg: '#f6f8fa',
  text: '#f0f6fc',
  arrow: '#59636e',
  label: '#1f2328',
  counterOk: '#2e7d32',
  counterFail: '#d32f2f',
  from: '#238636',
  to: '#1f6feb',
  eip: '#8957e5',
  choice: '#d29922',
  default: '#3d444d',
  transform: '#1b7c83',
  processor: '#bf4b8a',
  selectedStroke: '#0969da',
  badgeBg: '#59636e',
  badgeText: '#f6f8fa',
  tooltipBg: '#f6f8fa',
  tooltipText: '#1f2328',
  tooltipBorder: '#d1d7e0',
};

export function getNodeColor(type, theme) {
  if (type == null) return theme.default;
  switch (type) {
    case 'from':
      return theme.from;
    case 'to':
    case 'toD':
    case 'wireTap':
    case 'enrich':
    case 'pollEnrich':
      return theme.to;
    case 'choice':
    case 'when':
    case 'otherwise':
      return theme.choice;
    case 'marshal':
    case 'unmarshal':
    case 'transform':
    case 'setBody':
    case 'setHeader':
    case 'setProperty':
    case 'convertBodyTo':
    case 'removeHeader':
    case 'removeHeaders':
    case 'removeProperty':
    case 'removeProperties':
      return theme.transform;
    case 'bean':
    case 'process':
    case 'log':
    case 'script':
    case 'delay':
      return theme.processor;
    case 'filter':
    case 'split':
    case 'aggregate':
    case 'multicast':
    case 'recipientList':
    case 'routingSlip':
    case 'dynamicRouter':
    case 'loadBalance':
    case 'circuitBreaker':
    case 'saga':
    case 'doTry':
    case 'doCatch':
    case 'doFinally':
    case 'onException':
    case 'onCompletion':
    case 'intercept':
    case 'loop':
    case 'resequence':
    case 'throttle':
    case 'kamelet':
    case 'pipeline':
    case 'threads':
      return theme.eip;
    default:
      return theme.default;
  }
}

export function lightenColor(hex, amount = 0.2) {
  const r = parseInt(hex.slice(1, 3), 16);
  const g = parseInt(hex.slice(3, 5), 16);
  const b = parseInt(hex.slice(5, 7), 16);
  const lr = Math.min(255, Math.round(r + (255 - r) * amount));
  const lg = Math.min(255, Math.round(g + (255 - g) * amount));
  const lb = Math.min(255, Math.round(b + (255 - b) * amount));
  return `#${lr.toString(16).padStart(2, '0')}${lg.toString(16).padStart(2, '0')}${lb.toString(16).padStart(2, '0')}`;
}
