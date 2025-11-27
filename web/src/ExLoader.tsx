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

import React, { useState, useEffect, } from "react"
import ExPage from "./ExPage"
import { Proposition, ProjectOptions, SonarIssue, SonarSeachResult } from "./ExDataType"
import { transitionIssue, consolidate } from "./ExUtil"

interface Props {
  options?: ProjectOptions
  utils: any
}


const delay = (ms: number) => new Promise(res => setTimeout(res, ms));

const ExLoader: React.FC<Props> = (props) => {
  const [loading, setLoading] = useState(true)
  const [report, setReport] = useState<Proposition[]>([])
  const [issues, setIssues] = useState<SonarIssue[]>([])
  const [height, setHeight] = useState(0)

  async function asyncCall() {
    await delay(500);
    const json = await fetch("measure.json").then(r => r.json())
    const ijson = await fetch("issues.json").then(r => r.json())
    setReport(json as Proposition[])
    setIssues((ijson as SonarSeachResult).issues ?? [])
    setLoading(false)
  }

  const updateIssue = (issue: SonarIssue | null) => {
    if (issue && issue.status !== "OPEN") {
      let iss = issues.filter((i) => i.key !== issue.key)
      setIssues(iss)
    } else {
      alert("Can't transition issue")
    }
  }

  const updateStatus = (status: "falsepositive" | "accept", key?: string) => {
    if (key && status) {
      if (process.env.NODE_ENV === "production") {
        // eslint-disable-next-line react/prop-types
        console.log("Do transition ", status, key)
        props.utils.setDependencyExplorerIssueStatus(props.options!, status, key).then((data: SonarIssue) => {
          console.log("Transition result : ", data)
          updateIssue(data)
        })
      } else {
        updateIssue(transitionIssue(status, key, issues))
      }
    }
  }

  const loadingIssues = () => {
    props.utils.findDependencyExplorerIssues(props.options!).then((data: SonarIssue[]) => {
      setLoading(false)
      setIssues(data ?? [])
    })
  }

  useEffect(() => {
    if (process.env.NODE_ENV === "production") {
      // eslint-disable-next-line react/prop-types
      props.utils.findDependencyExplorerReport(props.options!).then((data: Proposition[]) => {
        setReport(data)
        loadingIssues()
      })
    } else {
      asyncCall()
    }
    /**
    * Add event listener
    */
    updateDimensions()
    window.addEventListener("resize", updateDimensions.bind(this))
  }, [])

  useEffect(() => {
    // componentWillUnmount
    return () => {
      window.removeEventListener("resize", updateDimensions.bind(this))
    }
  }, [height])

  const updateDimensions = () => {
    // 72px SonarQube common pane
    // 72px SonarQube project pane
    // 145,5 SonarQube footer
    let updateHeight = window.innerHeight - (72 + 48 + 145.5)
    setHeight(updateHeight)
  }

  return <>
    <div className="banner">Dependency explorer</div>
    <div className="page">
      {loading ? <div className="spinner"></div> : <ExPage data={consolidate(report, issues)} updateStatus={updateStatus} />}
    </div>
  </>
}
export default ExLoader