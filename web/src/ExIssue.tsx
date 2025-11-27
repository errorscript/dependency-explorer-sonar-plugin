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
import React from "react"
import { SeverityIcon } from "./ExBadge"
import { hashCode } from "./ExUtil"
import { Issue } from "./ExDataType"
import ExpandMoreIcon from "@mui/icons-material/ExpandMore"


interface Props {
    data: Issue[]
    mode: "incoherence" | "mismatch" | "unused" | "update" | "global" | "transitive" | null
    onSelect: (a: Issue) => void
    showTransitionDialog: (x: number, y: number, key?: string) => void
}

const ExIssueList: React.FC<Props> = (props) => {
    return <>
        {
            props.data.map((v) => (
                <ExIssue key={hashCode(v)} local={v} mode={props.mode} onSelect={props.onSelect} showTransitionDialog={props.showTransitionDialog} />
            ))
        }
    </>
}
export default ExIssueList

interface XProps {
    local: Issue
    mode: "incoherence" | "mismatch" | "unused" | "update" | "global" | "transitive" | null
    onSelect: (a: Issue) => void
    showTransitionDialog: (x: number, y: number, key?: string) => void
}

const ExIssue: React.FC<XProps> = (props) => {
    const handleSelect = () => {
        props.onSelect(props.local)
    }

    const showTransitionDialog = (e: React.MouseEvent<HTMLButtonElement, MouseEvent>, key?: string) => {
        let x = 0;
        let y = 0;
        if (e) {
            x = e.clientX
            y = (e.pageY - 204)
        }

        props.showTransitionDialog(x, y, key)
    }

    if (props.mode === "global" || props.mode === props.local.badge) {
        return <div role="button" className="issue-card" onClick={handleSelect} onKeyUp={handleSelect} style={{ border: 'none', backgroundColor: 'inherit' }}>
            <div className="issue-title">
                <div><SeverityIcon severity={props.local.severity} badge={props.local.badge} />&nbsp;{props.local.module}</div>
                <button
                    onClick={(e) => { showTransitionDialog(e, props.local.key); e.stopPropagation() }}
                    className="status_button"                        >
                    <span className="status_dropdown">
                        <span className="status_state">Open</span>
                        <span className="status_icon">
                            <ExpandMoreIcon fontSize="small" />
                        </span>
                    </span>
                </button>
            </div>
            <div className="issue-description">{props.local.description}</div>
        </div>
    } else {
        return <></>
    }
}

