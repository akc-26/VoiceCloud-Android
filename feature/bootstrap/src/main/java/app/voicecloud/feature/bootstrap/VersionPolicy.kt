package app.voicecloud.feature.bootstrap

object VersionPolicy {
    fun compare(left: String, right: String): Int {
        fun parts(v: String) = v.substringBefore('-').split('.').map { it.toIntOrNull() ?: 0 }
        val a=parts(left); val b=parts(right); val n=maxOf(a.size,b.size)
        for(i in 0 until n){ val d=(a.getOrElse(i){0}).compareTo(b.getOrElse(i){0}); if(d!=0)return d }
        return 0
    }
    fun requiresForceUpdate(current: String, minSupported: String?, forceUpdate: Boolean): Boolean = forceUpdate && minSupported != null && compare(current, minSupported) < 0
}
