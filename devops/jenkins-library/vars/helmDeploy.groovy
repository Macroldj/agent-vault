def call(Map args = [:]) {
  def shx = new org.company.ci.Shell(this)

  def release   = args.get('release', '')
  def chart     = args.get('chart', '')
  def namespace = args.get('namespace', 'default')
  def timeout   = args.get('timeout', '5m')
  def values    = args.get('values', [:]) as Map
  def valuesFile= args.get('valuesFile', '')

  if (!release || !chart) {
    error "helmDeploy: release/chart required"
  }

  def valFlags = values.collect { k, v -> "--set ${k}=${v}" }.join(' ')
  def vfFlag = valuesFile ? "-f ${valuesFile}" : ""

  shx.run("helm version || true")
  shx.run("""
    helm upgrade --install ${release} ${chart} \
      --namespace ${namespace} --create-namespace \
      --wait --timeout ${timeout} ${vfFlag} ${valFlags}
  """.stripIndent())
}