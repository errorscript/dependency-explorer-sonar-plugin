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
import React, { useState } from "react"
import ExModule from "./ExModule"
import ExIssueList from "./ExIssue"
import ExRuleList from "./ExRuleList"
import { hashCode, limitData } from "./ExUtil"
import { Proposition, ActiveArtifact } from "./ExDataType"


interface Props {
    data: Proposition | null
    updateStatus: (status: "falsepositive" | "accept", key?: string) => void
}

const ExPage: React.FC<Props> = (props) => {
    const getProjectList = () => {
        let data = props?.data?.dependencies ?? {}
        let list: { name: string }[] = []
        for (let name in data) {
            list.push({ name: name })
        }
        return list
    }
    const getData = () => {
        let data = props?.data?.dependencies ?? {}
        let issues = props?.data?.issues ?? []
        let dt: { [key: string]: ActiveArtifact } = {}
        for (let name in data) {
            dt[name] = limitData(issues, data[name][0])
        }
        return dt
    }

    const [menu, setMenu] = useState<boolean>(false)
    const [issueKey, setIssueKey] = useState<string>("")
    const data = getData()
    const openBadges = props?.data?.metadata?.openBadges ?? []
    const issues = props?.data?.issues ?? []
    const projectList = getProjectList()
    const [search, setSearch] = useState("")
    const [mode, setMode] = useState<"incoherence" | "mismatch" | "unused" | "update" | "global" | "transitive" | null>("global")
    const [modalPosition, setModalPosition] = useState({})

    const showTransitionDialog = (x: number, y: number, key?: string): void => {
        if (key) {
            setModalPosition({
                position: "absolute",
                top: y + "px",
                left: x + "px"
            })
            setIssueKey(key)
            setMenu(true)
        }
    }

    const updateStatus = (status: "falsepositive" | "accept", key?: string) => {
        props.updateStatus(status, key)
        setMenu(false)
    }

    const selectMode = (event: any) => {
        setMode(event.target.value)
        setSearch("")
    }

    const handleSearch = (event: any) => {
        setSearch(event.target.value)
    }

    const handleSelect = (event: any) => {
        setSearch(event.artifact)
        setMode(event.badge)
    }

    let llist = projectList ?? []
    if (llist.length === 0)
        return (
            <div className="page-main">
                <h2>No data</h2>
                <h3>See error during project scan, or allow explorer to scan this project.</h3>
            </div>
        )
    return (<>
        <div className="page-side">
            <div className="menu-modes">
                <h3>Rule</h3>
                <ExRuleList mode={mode} selectMode={selectMode} openBadges={openBadges} />
            </div>
            <div className="menu-search">
                <h3>Search</h3>
                <input id="search" type="text" value={search} onChange={handleSearch} />
            </div>
            <div className="menu-issues">
                <h3>Issues</h3>
                <ExIssueList data={issues} mode={mode} onSelect={handleSelect} showTransitionDialog={showTransitionDialog} />
            </div>
        </div>
        <div className="page-main">
            {
                llist.map((v) => (
                    <ExModule key={hashCode(v)} data={data} local={v} search={search} mode={mode} />
                ))
            }
        </div>
        <div role="button" tabIndex={-1} className="modal" style={{ display: menu ? "block" : "none" }} onClick={() => setMenu(false)}
            onKeyUp={() => setMenu(false)}></div>
        <div role="button" tabIndex={0} className="modal-content" style={{ ...modalPosition, display: menu ? "block" : "none" }} onClick={(e) => e.stopPropagation()}
            onKeyUp={(e) => e.stopPropagation()} >
            <div className="dropdown-menu">
                <div className="dropdown-menu-cartouche">
                    <div className="dropdown-menu-title">Change issue status</div>
                </div>
                <div className="dropdown-menu-separator"></div>
                <div className="dropdown-menu-item">
                    <button className="dropdown-button" onClick={() => updateStatus("accept", issueKey)}>
                        <div className="dropdown-button-title">Accept</div>
                        <div className="dropdown-button-description">Won't fix immediately, but will no longer affect the quality gate</div>
                    </button>
                </div>
                <div className="dropdown-menu-item">
                    <button className="dropdown-button" onClick={() => updateStatus("falsepositive", issueKey)}>
                        <div className="dropdown-button-title">False Positive</div>
                        <div className="dropdown-button-description">Analysis is incorrect</div>
                    </button>
                </div>
            </div>
        </div>
    </>)
}
export default ExPage


