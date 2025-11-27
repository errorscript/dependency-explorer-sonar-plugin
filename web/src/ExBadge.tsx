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
import React from 'react'
import DownloadIcon from '@mui/icons-material/Download'
import DangerousIcon from '@mui/icons-material/Dangerous'
import CopyrightIcon from '@mui/icons-material/Copyright'
import SignpostIcon from '@mui/icons-material/Signpost'
import MoveDownIcon from '@mui/icons-material/MoveDown'
import Inventory2OutlinedIcon from '@mui/icons-material/Inventory2Outlined'
import KeyboardArrowRightIcon from "@mui/icons-material/KeyboardArrowRight"
import ExpandMoreIcon from "@mui/icons-material/ExpandMore"
import BugReportOutlinedIcon from '@mui/icons-material/BugReportOutlined'
import ExtensionOutlinedIcon from '@mui/icons-material/ExtensionOutlined'
import Icon from '@mui/material/Icon'
import { OverridableStringUnion } from '@mui/types'
import { ActiveArtifact } from "./ExDataType"


const isDisabled = (value: boolean) => {
    return value ? "info" : "disabled"
}

interface XProps {
    update: boolean
    incoherence: boolean
    mismatch: boolean
    unused: boolean
    transitive: boolean
}

const BadgeRow: React.FC<XProps> = (props) => {
    return <div style={{ display: "flex", alignItems: "center" }}>
        <SeverityIcon badge="update" severity={isDisabled(props.update)} />
        <SeverityIcon badge="incoherence" severity={isDisabled(props.incoherence)} />
        <SeverityIcon badge="mismatch" severity={isDisabled(props.mismatch)} />
        <SeverityIcon badge="unused" severity={isDisabled(props.unused)} />
        <SeverityIcon badge="transitive" severity={isDisabled(props.transitive)} />
    </div>
}
export default BadgeRow

interface Props {
    severity: string
    badge: "incoherence" | "mismatch" | "unused" | "update" | "transitive" | null
}

export const  SeverityIcon: React.FC<Props> = (props) => {
    let color = severityColor(props.severity)
    if (props.badge === "mismatch") {
        return <CopyrightIcon fontSize="small" color={color} />
    } else if (props.badge === "incoherence") {
        return <SignpostIcon fontSize="small" color={color} />
    } else if (props.badge === "transitive") {
        return <MoveDownIcon fontSize="small" color={color} />
    } else if (props.badge === "update") {
        return <DownloadIcon fontSize="small" color={color} />
    } else if (props.badge === "unused") {
        return <DangerousIcon fontSize="small" color={color} />
    } else {
        return <></>
    }
}

const severityColor = function (severity: string): OverridableStringUnion<
    | 'inherit'
    | 'action'
    | 'disabled'
    | 'primary'
    | 'secondary'
    | 'error'
    | 'info'
    | 'success'
    | 'warning'
> {
    if (severity === "MINOR") {
        return "primary"
    } else if (severity === "MAJOR") {
        return "success"
    } else if (severity === "CRITICAL") {
        return "warning"
    } else if (severity === "BLOCKER") {
        return "error"
    } else if (severity === "disabled") {
        return "disabled"
    } else {
        return "info"
    }
}

export const DefaultIcon = (item: ActiveArtifact) => {
    return (
        <div style={{ display: "flex", alignItems: "center" }}>
            <Icon fontSize="small" />
            <PackageIcon type={item.type} />
        </div>
    )
}

export const RightIcon = (item: ActiveArtifact) => {
    return (
        <div style={{ display: "flex", alignItems: "center" }}>
            <KeyboardArrowRightIcon fontSize="small" />
            <PackageIcon type={item.type} />
        </div>
    )
}

export const DownIcon = (item: ActiveArtifact) => {
    return (
        <div style={{ display: "flex", alignItems: "center" }}>
            <ExpandMoreIcon fontSize="small" />
            <PackageIcon type={item.type} />
        </div>
    )
}

interface GProps {
    type: string
}

const PackageIcon: React.FC<GProps> = (props) => {
    if (props.type === "test") {
        return (<BugReportOutlinedIcon fontSize="small" />)
    }
    if (props.type === "plugin") {
        return (<ExtensionOutlinedIcon fontSize="small" />)
    }
    return (<Inventory2OutlinedIcon fontSize="small" />)
}
