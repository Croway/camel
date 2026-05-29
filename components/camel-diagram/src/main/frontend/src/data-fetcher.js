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

export async function fetchRouteStructure(src, metric, filter) {
  const url = new URL(src, window.location.href);
  url.searchParams.set('metric', String(metric));
  if (filter) url.searchParams.set('filter', filter);

  const resp = await fetch(url, {
    headers: { 'Accept': 'application/json' },
  });

  if (!resp.ok) {
    throw new Error(`Failed to fetch route structure: ${resp.status} ${resp.statusText}`);
  }

  return resp.json();
}

export class PollingController {
  constructor(host, fetchFn) {
    this._host = host;
    this._fetchFn = fetchFn;
    this._intervalId = null;
  }

  start(intervalSeconds) {
    this.stop();
    if (intervalSeconds > 0) {
      this._intervalId = setInterval(() => this._fetchFn(), intervalSeconds * 1000);
    }
  }

  stop() {
    if (this._intervalId != null) {
      clearInterval(this._intervalId);
      this._intervalId = null;
    }
  }
}
