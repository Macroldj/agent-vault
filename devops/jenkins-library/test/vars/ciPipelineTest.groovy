import org.junit.Before
import org.junit.Test
import static org.junit.Assert.*
import com.lesfurets.jenkins.unit.BasePipelineTest

class ciPipelineTest extends BasePipelineTest {
  @Before
  void setUp() throws Exception {
    super.setUp()
    helper.registerAllowedMethod('dockerBuild', [Map.class], { m -> println "dockerBuild called: ${m}" })
    helper.registerAllowedMethod('helmDeploy', [Map.class], { m -> println "helmDeploy called: ${m}" })
  }

  @Test
  void test_call_minimal() {
    def script = loadScript('vars/ciPipeline.groovy')
    script.call([ language: 'go', projectDir: '.' ])
    printCallStack()
    assertJobStatusSuccess()
  }
}