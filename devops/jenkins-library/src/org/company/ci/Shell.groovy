package org.company.ci

class Shell implements Serializable {
  private final def steps
  Shell(def steps) { this.steps = steps }

  def run(String cmd, int retries = 0, int sleepSec = 5) {
    int attempt = 0
    while (true) {
      try {
        return steps.sh(label: "sh", script: """#!/usr/bin/env bash
          set -euo pipefail
          ${cmd}
        """.stripIndent())
      } catch (Throwable t) {
        if (attempt >= retries) throw t
        attempt++
        steps.echo "Retry ${attempt}/${retries} after error: ${t.message}"
        steps.sleep time: sleepSec, unit: 'SECONDS'
      }
    }
  }
}