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
import { ReactElement } from "react"


export type Artifact = {
    artifactId: string
    groupId: string
    lastVersion?: string
    level: number
    licenses?: string[] | string
    nextVersion?: string
    source?: string
    scope?: string
    version?: string
    propertyName?: string
    children?: Artifact[]
}

export type ActiveArtifact = {
    id: string
    name: string
    version?: string
    propertyName?: string
    nextVersion?: string
    lastVersion?: string
    licenses?: string
    type: string
    incoherence: boolean
    mismatch: boolean
    hasSubMismatch: boolean
    unused: boolean
    transitive: boolean
    hasSubTransitive: boolean
    hasSubIncoherence: boolean
    badges: ReactElement
    nodes?: ActiveArtifact[]
    parent?: ActiveArtifact | null
    addedToParent?: boolean
}

export type BranchLike = {
    name: string
    key: string
}

export type Component = {
    key: string
}

export type Issue = {
    key?: string;
    artifact: string
    badge: "incoherence" | "mismatch" | "unused" | "update" | "transitive" | null
    description: string
    module: string
    severity: string
    status?: string
}

export type Metadata = {
    openBadges: string[]
}

export type ProjectDependencies = {
    [key: string]: Artifact[]
}

export type ProjectOptions = {
    branchLike: BranchLike
    component: Component
}

export type Proposition = {
    dependencies?: ProjectDependencies
    issues?: Issue[]
    metadata?: Metadata
}

export type Request = {
    component: string
    metricKeys: string
    branch?: string
    pullRequest?: string
}



export type SonarPaging = {
    pageIndex: number;
    pageSize: number;
    total: number;
}

export type SonarIssue = {
    key: string;
    rule: string;
    severity: string;
    component: string;
    project: string;
    status: string;
    message: string;
    flows?: [],
    author?: string;
    tags?: string[];
    transitions?: string[];
    actions?: string[];
    comments?: string[],
    creationDate?: string;
    updateDate?: string;
    type?: string;
    scope?: string;
    quickFixAvailable?: boolean;
    messageFormattings?: string[],
    codeVariants?: string[],
    cleanCodeAttribute?: string;
    cleanCodeAttributeCategory?: string[]
    impacts?: SonarImpact[];
    issueStatus?: string;
    prioritizedRule?: boolean;
    fromSonarQubeUpdate?: boolean;
    internalTags?: string[];
}

export type SonarImpact = {
    softwareQuality: string;
    severity: string;
}

export type SonarRule = {
    key: string;
    name: string;
    lang: string;
    status: string;
    langName: string;
}


export type SonarSeachResult = {
    total: number;
    p: number;
    ps: number;
    paging: SonarPaging;
    effortTotal: number;
    issues: SonarIssue[];
    components: any[];
    rules: SonarRule[];
    users: any[];
    languages: any[];
    facets: any[];
}

export type SonarTransitionResult = {
    issues: SonarIssue;
    components: any[];
    rules: SonarRule[];
    users: any[];
}


export type SonarUpdateStatus = {

}