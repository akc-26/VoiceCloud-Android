package app.voicecloud.feature.bootstrap

import org.junit.Assert.*
import org.junit.Test

class VersionPolicyTest {
    @Test fun semanticComparisonIsNumeric() { assertTrue(VersionPolicy.compare("1.10.0","1.9.9") > 0); assertEquals(0,VersionPolicy.compare("1.0","1.0.0")) }
    @Test fun forceUpdateRequiresBothFlagAndMinimumViolation() { assertTrue(VersionPolicy.requiresForceUpdate("1.0.0","1.1.0",true)); assertFalse(VersionPolicy.requiresForceUpdate("1.2.0","1.1.0",true)); assertFalse(VersionPolicy.requiresForceUpdate("1.0.0","1.1.0",false)) }
}
