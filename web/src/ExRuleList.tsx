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
import ToggleButton from '@mui/material/ToggleButton'
import ToggleButtonGroup from '@mui/material/ToggleButtonGroup'
import { hashCode } from "./ExUtil"


interface Props {
    mode: "incoherence" | "mismatch" | "unused" | "update" | "global" | "transitive" | null
    selectMode: (event: any) => void
    openBadges: string[]
}

const ExRuleList: React.FC<Props> = (props) => {
    return <ToggleButtonGroup orientation="vertical" size="small"
        value={props.mode}
        exclusive
        onChange={props.selectMode}>
        <ToggleButton value='global'>All rules</ToggleButton>
        {
            props.openBadges.map((v) => {
                if (v === "update") {
                    return <ToggleButton key={hashCode(v)} value='update'>Updates</ToggleButton>
                } else if (v === "incoherence") {
                    return <ToggleButton key={hashCode(v)} value='incoherence'>Version incoherence</ToggleButton>
                } else if (v === "mismatch") {
                    return <ToggleButton key={hashCode(v)} value='mismatch'>Licenses mismatch</ToggleButton>
                } else if (v === "unused") {
                    return <ToggleButton key={hashCode(v)} value='unused'>Unused dependency</ToggleButton>
                } else if (v === "transitive") {
                    return <ToggleButton key={hashCode(v)} value='transitive'>Transitive usage</ToggleButton>
                } else return <></>
            })
        }
    </ToggleButtonGroup>
}
export default ExRuleList