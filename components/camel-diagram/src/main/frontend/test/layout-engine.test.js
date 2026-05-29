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

import { expect } from '@esm-bundle/chai';
import {
  buildTree, layoutRoute, isBranchingEip, hasScope,
  cleanLabel, resolveLabel, parseRoutes, expandBoundsForBox,
  V_GAP, PADDING, DEFAULT_BOX_WIDTH, DEFAULT_FONT_SIZE,
} from '../src/layout-engine.js';

function node(type, code, level, description) {
  return { type, code, description: description || null, level, id: null, stat: null };
}

function nodeWithId(type, code, level, id) {
  return { type, code, description: null, level, id, stat: null };
}

function nodeWithStat(type, code, level, total, failed) {
  return { type, code, description: null, level, id: null, stat: { exchangesTotal: total, exchangesFailed: failed } };
}

describe('buildTree', () => {
  it('returns null for empty nodes', () => {
    expect(buildTree([])).to.be.null;
  });

  it('builds single node', () => {
    const root = buildTree([node('from', 'timer:tick', 0)]);
    expect(root).to.not.be.null;
    expect(root.info.type).to.equal('from');
    expect(root.children).to.have.length(0);
  });

  it('builds sequential tree', () => {
    const root = buildTree([
      node('from', 'timer:tick', 0),
      node('to', 'log:a', 1),
      node('to', 'log:b', 1),
    ]);
    expect(root).to.not.be.null;
    expect(root.children).to.have.length(2);
    expect(root.children[0].info.code).to.equal('log:a');
    expect(root.children[1].info.code).to.equal('log:b');
  });

  it('builds branching tree', () => {
    const root = buildTree([
      node('from', 'timer:tick', 0),
      node('choice', 'choice()', 1),
      node('when', 'when(simple(...))', 2),
      node('to', 'log:a', 3),
      node('otherwise', 'otherwise()', 2),
      node('to', 'log:b', 3),
    ]);
    expect(root).to.not.be.null;
    expect(root.children).to.have.length(1);
    const choice = root.children[0];
    expect(choice.info.type).to.equal('choice');
    expect(choice.children).to.have.length(2);
    expect(choice.children[0].info.type).to.equal('when');
    expect(choice.children[1].info.type).to.equal('otherwise');
    expect(choice.children[0].children).to.have.length(1);
    expect(choice.children[1].children).to.have.length(1);
  });

  it('walks up multiple levels', () => {
    const root = buildTree([
      node('from', 'timer:tick', 0),
      node('choice', 'choice()', 1),
      node('when', 'when(...)', 2),
      node('to', 'log:deep', 3),
      node('to', 'log:after-choice', 1),
    ]);
    expect(root).to.not.be.null;
    expect(root.children).to.have.length(2);
    expect(root.children[0].info.type).to.equal('choice');
    expect(root.children[1].info.code).to.equal('log:after-choice');
  });

  it('handles deeply nested choices', () => {
    const root = buildTree([
      node('from', 'timer:tick', 0),
      node('choice', 'choice()', 1),
      node('when', 'when(a)', 2),
      node('choice', 'choice()', 3),
      node('when', 'when(b)', 4),
      node('to', 'log:deep', 5),
      node('to', 'log:end', 1),
    ]);
    expect(root).to.not.be.null;
    expect(root.children).to.have.length(2);
    const outerChoice = root.children[0];
    expect(outerChoice.info.type).to.equal('choice');
    const when = outerChoice.children[0];
    expect(when.children).to.have.length(1);
    const innerChoice = when.children[0];
    expect(innerChoice.info.type).to.equal('choice');
    expect(root.children[1].info.code).to.equal('log:end');
  });
});

describe('cleanLabel', () => {
  it('returns empty for null', () => {
    expect(cleanLabel(null)).to.equal('');
  });

  it('preserves normal labels', () => {
    expect(cleanLabel('log:hello')).to.equal('log:hello');
  });

  it('strips leading dot', () => {
    expect(cleanLabel('.to("log:a")')).to.equal('to("log:a")');
  });
});

describe('resolveLabel', () => {
  it('returns code by default', () => {
    const result = resolveLabel({ code: 'timer:tick', description: 'Poll every second' }, 'code');
    expect(result).to.deep.equal(['timer:tick']);
  });

  it('returns description when mode is description', () => {
    const result = resolveLabel({ code: 'timer:tick', description: 'Poll every second' }, 'description');
    expect(result).to.deep.equal(['Poll every second']);
  });

  it('falls back to code when description is null', () => {
    const result = resolveLabel({ code: 'timer:tick', description: null }, 'description');
    expect(result).to.deep.equal(['timer:tick']);
  });

  it('returns both when mode is both', () => {
    const result = resolveLabel({ code: 'timer:tick', description: 'Poll every second' }, 'both');
    expect(result).to.deep.equal(['Poll every second', 'timer:tick']);
  });

  it('returns just code when description equals code in both mode', () => {
    const result = resolveLabel({ code: 'timer:tick', description: 'timer:tick' }, 'both');
    expect(result).to.deep.equal(['timer:tick']);
  });
});

describe('isBranchingEip', () => {
  it('identifies branching EIPs', () => {
    expect(isBranchingEip('choice')).to.be.true;
    expect(isBranchingEip('multicast')).to.be.true;
    expect(isBranchingEip('doTry')).to.be.true;
    expect(isBranchingEip('loadBalance')).to.be.true;
    expect(isBranchingEip('recipientList')).to.be.true;
    expect(isBranchingEip('circuitBreaker')).to.be.true;
  });

  it('rejects non-branching types', () => {
    expect(isBranchingEip('to')).to.be.false;
    expect(isBranchingEip('from')).to.be.false;
    expect(isBranchingEip('log')).to.be.false;
    expect(isBranchingEip(null)).to.be.false;
  });
});

describe('layoutRoute', () => {
  it('lays out a single route', () => {
    const route = {
      routeId: 'route1',
      source: null,
      nodes: [
        node('from', 'timer:tick', 0),
        node('to', 'log:a', 1),
      ],
    };
    const lr = layoutRoute(route, PADDING);

    expect(lr.nodes).to.have.length(2);
    expect(lr.maxX).to.be.greaterThan(0);
    expect(lr.maxY).to.be.greaterThan(0);

    const fromNode = lr.nodes[0];
    const toNode = lr.nodes[1];
    expect(fromNode.type).to.equal('from');
    expect(toNode.type).to.equal('to');
    expect(fromNode.x).to.equal(toNode.x);
    expect(toNode.y).to.be.greaterThan(fromNode.y);
  });

  it('lays out a branching route with different X positions', () => {
    const route = {
      routeId: 'route1',
      source: null,
      nodes: [
        node('from', 'timer:tick', 0),
        node('choice', 'choice()', 1),
        node('when', 'when(...)', 2),
        node('to', 'log:a', 3),
        node('otherwise', 'otherwise()', 2),
        node('to', 'log:b', 3),
      ],
    };
    const lr = layoutRoute(route, PADDING);

    expect(lr.nodes).to.have.length(6);
    const whenNode = lr.nodes[2];
    const otherwiseNode = lr.nodes[4];
    expect(whenNode.x).to.not.equal(otherwiseNode.x);
  });

  it('handles empty route', () => {
    const route = { routeId: 'empty', source: null, nodes: [] };
    const lr = layoutRoute(route, 0);

    expect(lr.nodes).to.have.length(0);
    expect(lr.maxX).to.be.greaterThan(0);
  });

  it('sets connectFromMerge for node after filter scope', () => {
    const route = {
      routeId: 'route1',
      source: null,
      nodes: [
        node('from', 'direct:start', 0),
        node('setBody', 'setBody[simple{Hello}]', 1),
        node('filter', 'filter[{header(x) == value}]', 1),
        node('removeHeader', 'removeHeader[x-another-header]', 2),
        node('to', 'to[log:after]', 1),
      ],
    };
    const lr = layoutRoute(route, PADDING);

    expect(lr.nodes).to.have.length(5);
    const toNode = lr.nodes[4];
    expect(toNode.connectFromMerge).to.be.true;
  });

  it('sets connectFromMerge for node after split scope', () => {
    const route = {
      routeId: 'route1',
      source: null,
      nodes: [
        node('from', 'direct:start', 0),
        node('split', 'split[body()]', 1),
        node('log', 'log[${body}]', 2),
        node('to', 'to[mock:end]', 1),
      ],
    };
    const lr = layoutRoute(route, PADDING);

    const toNode = lr.nodes[3];
    expect(toNode.connectFromMerge).to.be.true;
  });

  it('sets connectFromMerge for doTry with choice inside', () => {
    const route = {
      routeId: 'route1',
      source: null,
      nodes: [
        node('from', 'timer:tryChoiceInside', 0),
        node('setHeader', 'setHeader[type]', 1),
        node('doTry', 'doTry', 1),
        node('choice', 'choice()', 2),
        node('when', 'when(header(type) == A)', 3),
        node('log', 'log[Type A]', 4),
        node('otherwise', 'otherwise()', 3),
        node('log', 'log[Other type]', 4),
        node('throwException', 'throwException[java.lang.Exception]', 4),
        node('doCatch', 'doCatch[java.lang.Exception]', 2),
        node('log', 'log[Err: ${exception.message}]', 3),
        node('log', 'log[Do other processing...]', 1),
      ],
    };
    const lr = layoutRoute(route, PADDING);

    const logAfter = lr.nodes[lr.nodes.length - 1];
    expect(logAfter.wrappedLines.join('')).to.equal('log[Do other processing...]');
    expect(logAfter.connectFromMerge).to.be.true;
  });

  it('positions next node after wrapped node correctly', () => {
    const route = {
      routeId: 'route1',
      source: null,
      nodes: [
        node('from', 'kafka:my-topic?brokers=localhost:9092&groupId=myConsumerGroup&autoOffsetReset=earliest', 0),
        node('to', 'log:a', 1),
      ],
    };
    const lr = layoutRoute(route, PADDING);
    const fromNode = lr.nodes[0];
    const toNode = lr.nodes[1];
    expect(toNode.y).to.equal(fromNode.y + fromNode.height + V_GAP);
  });

  it('uses description in description mode', () => {
    const route = {
      routeId: 'route1',
      source: null,
      nodes: [
        node('from', 'timer:tick?period=1000', 0, 'Poll every second'),
        node('to', 'log:a', 1),
      ],
    };
    const lr = layoutRoute(route, PADDING, { nodeLabel: 'description' });
    expect(lr.nodes[0].wrappedLines.join('')).to.equal('Poll every second');
    expect(lr.nodes[1].wrappedLines.join('')).to.equal('log:a');
  });

  it('propagates node ids', () => {
    const route = {
      routeId: 'route1',
      source: null,
      nodes: [
        nodeWithId('from', 'timer:tick', 0, 'from1'),
        nodeWithId('to', 'log:a', 1, 'to1'),
      ],
    };
    const lr = layoutRoute(route, PADDING);
    expect(lr.nodes[0].id).to.equal('from1');
    expect(lr.nodes[1].id).to.equal('to1');
  });
});

describe('parseRoutes', () => {
  it('parses empty routes', () => {
    expect(parseRoutes({ routes: [] })).to.have.length(0);
  });

  it('parses missing routes key', () => {
    expect(parseRoutes({})).to.have.length(0);
  });

  it('parses route with nodes', () => {
    const json = {
      routes: [{
        routeId: 'myRoute',
        source: 'file:/path/route.yaml',
        code: [
          { type: 'route', id: 'route1', level: 0, code: 'route' },
          { type: 'from', id: 'from1', level: 1, code: 'timer:tick' },
          { type: 'to', id: 'to1', level: 2, code: 'log:a' },
        ],
      }],
    };
    const routes = parseRoutes(json);
    expect(routes).to.have.length(1);
    expect(routes[0].routeId).to.equal('myRoute');
    expect(routes[0].source).to.equal('route.yaml');
    expect(routes[0].nodes).to.have.length(3);
    expect(routes[0].nodes[1].type).to.equal('from');
    expect(routes[0].nodes[1].code).to.equal('timer:tick');
  });

  it('parses route with statistics', () => {
    const json = {
      routes: [{
        routeId: 'myRoute',
        source: null,
        code: [{
          type: 'to', id: 'to1', level: 1, code: 'log:a',
          statistics: { exchangesTotal: 100, exchangesFailed: 5 },
        }],
      }],
    };
    const routes = parseRoutes(json);
    expect(routes[0].nodes[0].stat.exchangesTotal).to.equal(100);
    expect(routes[0].nodes[0].stat.exchangesFailed).to.equal(5);
  });
});
