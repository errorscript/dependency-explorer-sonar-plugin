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
import { Table, Header, HeaderRow, Body, Row, HeaderCell, Cell, } from "@table-library/react-table-library/table"
import { useTree, CellTree } from "@table-library/react-table-library/tree"
import FolderCopyOutlinedIcon from '@mui/icons-material/FolderCopyOutlined'
import { useTheme } from "@table-library/react-table-library/theme"
import { ActiveArtifact } from "./ExDataType"
import { Data } from '@table-library/react-table-library/types/table'
import { DefaultIcon, RightIcon, DownIcon } from "./ExBadge"
import { hasNextVersion, toData } from "./ExUtil"


interface XProps {
    data: { [key: string]: ActiveArtifact }
    local: {
        name: string
    }
    search: string
    mode: "incoherence" | "mismatch" | "unused" | "update" | "global" | "transitive" | null
}

const ExModule: React.FC<XProps> = (props) => {
    let dt = props.data[props.local.name]
    let filter = (item: ActiveArtifact): boolean => {
        let valid: boolean = props.mode !== 'global'
        if (props.search.length > 0)
            valid = valid && item.name.toLowerCase().includes(props.search.toLowerCase())
        if (props.mode) {
            switch (props.mode) {
                case "update":
                    valid = valid && hasNextVersion(item)
                    break
                case "mismatch":
                    valid = valid && item.mismatch || item.hasSubMismatch
                    break
                case "unused":
                    valid = valid && item.unused
                    break
                case "transitive":
                    valid = valid && item.transitive || item.hasSubTransitive
                    break
                case "incoherence":
                    valid = valid && item.incoherence || item.hasSubIncoherence
                    break
                case "global":
                    valid = valid || hasNextVersion(item)
                    valid = valid || item.incoherence || item.hasSubIncoherence
                    valid = valid || item.mismatch || item.hasSubMismatch
                    valid = valid || item.unused
                    valid = valid || item.transitive || item.hasSubTransitive
                    break
                default:
                    break
            }
        }
        return valid
    }

    let groupedData = toData(dt, filter, props.mode === 'global')
    let list = [groupedData.data]
    let data = { nodes: list }
    let openned = groupedData.openned
    return (<div className="project-module">
        <div className="project-module-title">
            <h2 style={{ display: "flex", alignItems: "center", verticalAlign: "top" }}>&nbsp;<FolderCopyOutlinedIcon fontSize="small" />&nbsp;{props.local.name}</h2>
        </div>
        <ExModuleTable data={data} toOpen={openned} />
    </div>)
}
export default ExModule

interface YProps {
    data: { nodes: (ActiveArtifact | null)[] }
    toOpen: string[]
}

const ExModuleTable: React.FC<YProps> = (props) => {
    let data = props.data
    const tree = useTree(data as Data<ActiveArtifact>, {
        state: {
            ids: props.toOpen,
        },
    }, {
        treeIcon: {
            iconDefault: DefaultIcon,
            iconRight: RightIcon,
            iconDown: DownIcon,
        },
    })
    const theme = useTheme({
        Table: `
--data-table-library_grid-template-columns:  calc(46% - 110px) 110px 8% 10% 8% 8% 20%;
`,
        HeaderRow: `
background-color: #eaf5fd;
.th {
  border-bottom: 1px solid #a0a8ae;
}
`,
        Row: `
&:nth-of-type(odd) {
  background-color: #d2e9fb;
}
&:nth-of-type(even) {
  background-color: #eaf5fd;
}
`,
        BaseCell: `
&:not(:last-of-type) {
  border-right: 1px solid #a0a8ae;
}
text-align: center;
&:first-of-type {
  text-align: left;
}
`,
    })
    return (
        <Table data={data} tree={tree} theme={theme} layout={{ custom: true }}>
            {(tableList: ActiveArtifact[]) => (
                <>
                    <Header>
                        <HeaderRow>
                            <HeaderCell resize>Name</HeaderCell>
                            <HeaderCell></HeaderCell>
                            <HeaderCell resize>Version</HeaderCell>
                            <HeaderCell resize>Property<br />name</HeaderCell>
                            <HeaderCell resize>Next<br />version</HeaderCell>
                            <HeaderCell resize>Last<br />version</HeaderCell>
                            <HeaderCell>Licenses</HeaderCell>
                        </HeaderRow>
                    </Header>
                    <Body>
                        {tableList.map((item: ActiveArtifact) => (
                            <Row key={item.id} item={item}>
                                <CellTree item={item}>{item.name}</CellTree>
                                <Cell>{item.badges}</Cell>
                                <Cell>{item.version}</Cell>
                                <Cell>{item.propertyName}</Cell>
                                <Cell>{item.nextVersion}</Cell>
                                <Cell>{item.lastVersion}</Cell>
                                <Cell>{item.licenses}</Cell>
                            </Row>
                        ))}
                    </Body>
                </>
            )}
        </Table>
    )
}