package org.company.ci

class Utils implements Serializable {
  private final def steps
  Utils(def steps) { this.steps = steps }

  def archiveIfExists(String path) {
    steps.sh(returnStatus: true, script: "[[ -e '${path}' ]]")
    if (steps.fileExists(path)) {
      steps.archiveArtifacts artifacts: path, onlyIfSuccessful: false
    }
  }

  def enforceCoverage(String coverFile, int threshold) {
    if (!steps.fileExists(coverFile) || threshold <= 0) return
    // 简化：示例读取 go 覆盖率，实际可解析行/函数覆盖率
    steps.echo "Enforcing coverage >= ${threshold}% (demo)"
  }
}