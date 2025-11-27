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
import { ActiveArtifact, Artifact, Issue, Proposition, SonarIssue } from "./ExDataType"
import BadgeRow from "./ExBadge"


export function hashCode(obj: any): number {
  let str = JSON.stringify(obj)
  let hash = 0
  if (str.length === 0) return hash
  for (let i = 0; i < str.length; i++) {
    let chr = str.codePointAt(i)
    hash = ((hash << 5) - hash) + (chr ?? 0)
    hash = Math.trunc(hash) // Convert to 32bit integer
  }
  return hash
}

function itemCompare(a: ActiveArtifact, b: ActiveArtifact) {
  if (a.name) {
    return b.name ? a.name.localeCompare(b.name) : -1
  }
  return b.name ? 1 : 0
}

function setParents(getter: (f: ActiveArtifact) => boolean, node: ActiveArtifact, setter: (f: ActiveArtifact) => void) {
  if (getter(node)) {
    let nd = node.parent
    while (nd) {
      setter(nd)
      nd = nd.parent
    }
  }
}

function addUp(root: ActiveArtifact, list: Set<string>, l: number) {
  let level = l
  let parent = root.parent
  let current = root
  while (parent) {
    parent.nodes ??= []
    if (!current.addedToParent) {
      parent.nodes.push(current)
      parent.nodes.sort(itemCompare)
      current.addedToParent = true
    }
    if (level < 3) {
      list.add(parent.id)
    }
    level--
    current = parent
    parent = parent.parent
  }
}

type MetaNode = {
  current: ActiveArtifact
  parent: ActiveArtifact | null
  level: number
}

function arrayToString(data?: string[] | string): string {
  if (!data) return ""
  if (Array.isArray(data)) {
    let build = ""
    for (const str of data) {
      if (build.length > 0)
        build += ", "
      build += str
    }
    return build
  } else { return data }
}

export function toData(root: ActiveArtifact, filter: (a: ActiveArtifact) => boolean, global: boolean): { data: ActiveArtifact | null, openned: string[] } {
  let stack: MetaNode[] = []
  let list = new Set<string>()
  let result = null
  stack.push({ parent: null, current: root, level: 0 })
  while (stack.length > 0) {
    let metaNode = stack.shift()
    let currentNode = metaNode!.current
    let node: ActiveArtifact = {
      id: currentNode.id,
      name: currentNode.name,
      version: currentNode.version,
      propertyName: currentNode.propertyName,
      nextVersion: currentNode.nextVersion,
      lastVersion: currentNode.lastVersion,
      licenses: arrayToString(currentNode.licenses),
      type: currentNode.type,
      incoherence: currentNode.incoherence,
      mismatch: currentNode.mismatch,
      hasSubMismatch: currentNode.hasSubMismatch,
      unused: currentNode.unused,
      transitive: currentNode.transitive,
      hasSubTransitive: currentNode.hasSubTransitive,
      hasSubIncoherence: currentNode.hasSubIncoherence,
      badges: currentNode.badges,
      parent: metaNode!.parent,
      addedToParent: false
    }
    setParents(f => f.mismatch, node, f => f.hasSubMismatch = true)
    setParents(f => f.transitive, node, f => f.hasSubTransitive = true)
    setParents(f => f.incoherence, node, f => f.hasSubIncoherence = true)
    result ??= node
    if (filter(node) || global) {
      addUp(node, list, metaNode!.level)
    }
    if (currentNode.nodes && currentNode.nodes?.length > 0) {
      for (const item of currentNode.nodes) {
        stack.unshift(
          { parent: node, current: item, level: metaNode!.level + 1 })
      }
    }
  }
  let op: string[] = []
  for (const item of list) {
    op.push(item)
  }
  op.sort((a, b) => a.localeCompare(b))
  return { data: result, openned: op }
}

export function hasNextVersion(item: ActiveArtifact): boolean {
  if (item.nextVersion)
    return item.nextVersion.length > 0
  return false
}

type Badges = {
  incoherence: boolean
  mismatch: boolean
  unused: boolean
  transitive: boolean
  update: boolean
}

const isInIssue = (ga: string, issues: Issue[]): Badges => {
  let bdg = {
    incoherence: false,
    mismatch: false,
    unused: false,
    transitive: false,
    update: false
  }
  for (const issue of issues ?? []) {
    if (issue.artifact.startsWith(ga) && issue.badge) {
      bdg[issue.badge] = true
    }
  }
  return bdg;
}

export function limitData(issues: Issue[], t: Artifact, p: string = "", d: number = 0, level: number = 1): ActiveArtifact {
  let _id = (p ? p + "." : "") + d
  let _type = "runtime"
  if (t.source?.includes("PLUGIN"))
    _type = "plugin"
  else if (t.scope && t.scope === "test")
    _type = "test"
  const name = t.groupId + ":" + t.artifactId
  const bdg = isInIssue(name, issues)
  let r: ActiveArtifact = {
    id: _id,
    name: name,
    version: t.version,
    propertyName: t.propertyName,
    nextVersion: t.nextVersion,
    lastVersion: t.lastVersion,
    licenses: arrayToString(t.licenses),
    type: _type,
    incoherence: bdg.incoherence,
    mismatch: bdg.mismatch,
    unused: bdg.unused,
    transitive: bdg.transitive,
    hasSubMismatch: false,
    hasSubTransitive: false,
    hasSubIncoherence: false,
    badges: <BadgeRow
      update={bdg.update}
      incoherence={bdg.incoherence}
      mismatch={bdg.mismatch}
      unused={bdg.unused}
      transitive={bdg.transitive}
    />
  }
  if (t.children && t.children.length > 0) {
    let _nodes: ActiveArtifact[] = []
    for (let i = 0; i < t.children.length; i++) {
      let s = limitData(issues, t.children[i], _id, i, level + 1)
      if (s)
        _nodes.push(s)
    }
    r.nodes = _nodes
  }
  return r
}

export function consolidate(props: Proposition[] | Proposition, issues: SonarIssue[]): Proposition | null {
  if (!props)
    return null
  if (!Array.isArray(props))
    return props
  if (props.length == 0)
    return null
  let result: Proposition = {
    dependencies: {},
    issues: [],
    metadata: { openBadges: [] }
  }
  for (const prop of props) {
    if (prop.dependencies)
      for (const property in prop.dependencies) {
        result.dependencies![property] = prop.dependencies[property]
      }
  }
  const mdSet = new Set();
  if (issues) {
    for (const issue of issues) {
      const badge = getBadge(issue.rule)
      mdSet.add(badge)
      result.issues?.push({
        key: issue.key,
        artifact: getArtefact(issue.message),
        badge: badge,
        description: issue.message,
        module: getModule(issue.component),
        severity: issue.severity,
        status: issue.issueStatus
      })
    }
  }
  result.metadata = {
    openBadges: [...mdSet] as string[]
  }
  return result
}

const getBadge = (rule: string): "incoherence" |
  "mismatch" |
  "unused" |
  "update" |
  "transitive" | null => {
  switch (rule) {
    case "DependencyExplorer:UsingOutdatedDependency":
      return "update";
    case "DependencyExplorer:UsingIncompatibleLicencedDependency":
      return "mismatch";
    case "DependencyExplorer:UsingIncoherentVersionnedDependency":
      return "incoherence";
    case "DependencyExplorer:UnusedDependency":
      return "unused";
    case "DependencyExplorer:UsingTransitiveDependency":
      return "transitive";
  }
  return null
}

const varRegex = /[\w.-]+:[\w-.]+/

const getArtefact = (desc: string): string => {
  let varArr = desc.match(varRegex);
  if (varArr && varArr.length >= 1) {
    return varArr[0];
  }
  return "";
}

const getModule = (desc: string): string => {
  let sn = desc.split(":")
  if (sn.length >= 4)
    return sn[0] + ":" + sn[2]
  if (sn.length >= 3)
    return sn[0] + ":" + sn[1]
  return desc
}

export function transitionIssue(status: string, key: string, issues: SonarIssue[]): SonarIssue | null {
  if (!issues)
    return null;
  if (status && key) {
    let issue: SonarIssue | undefined = issues.find((i) => i.key === key);
    if (issue) {
      let newIssue: SonarIssue = { ...issue, status: status.toLocaleUpperCase() }
      return newIssue
    }
  }
  return null
}
