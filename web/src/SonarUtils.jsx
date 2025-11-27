/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2023-2025 errorscript@gmail.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import { getJSON, postJSON } from "sonar-request";

export function isBranch(branchLike) {
  return branchLike !== undefined && branchLike.isMain !== undefined;
}

export function isPullRequest(branchLike) {
  return branchLike !== undefined && branchLike.key !== undefined;
}


export function findDependencyExplorerReport(options) {
  let request = {
    component: options.component.key,
    metricKeys: "ExplorerReport"
  }

  // branch and pullRequest are internal parameters for /api/measures/component
  if (isBranch(options.branchLike)) {
    request.branch = options.branchLike.name
  } else if (isPullRequest(options.branchLike)) {
    request.pullRequest = options.branchLike.key
  }

  return getJSON("/api/measures/component", request).then(function (response) {
    let report = response.component.measures.find((measure) => {
      return measure.metric === "ExplorerReport"
    })
    if (report?.value) {
      return JSON.parse(report.value)
    } else {
      return []
    }
  })
}

export function findDependencyExplorerIssues(options) {
  let request = {
    components: options.component.key,
    s: "FILE_LINE",
    issueStatuses: "CONFIRMED,OPEN",
    additionalFields: "_all",
    languages: "dependencyexplorer"
  }

  return getJSON("/api/issues/search", request).then(function (response) {
    let report = response.issues
    if (report) {
      return report
    } else {
      return []
    }
  })
}

export function setDependencyExplorerIssueStatus(options, status, key) {
  let request = {
    issue: key,
    transition: status,
  }

  return postJSON("/api/issues/do_transition", request).then(function (response) {
    let report = response.issue
    if (report) {
      return report
    } else {
      return null
    }
  })
}