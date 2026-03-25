import com.zegocloud.uikit.prebuilt.livestreaming.ZegoUIKitPrebuiltLiveStreamingConfig
import com.zegocloud.uikit.prebuilt.livestreaming.ZegoUIKitPrebuiltLiveStreamingFragment
import java.io.File

fun main() {
    val configClass = ZegoUIKitPrebuiltLiveStreamingConfig::class.java
    val fragmentClass = ZegoUIKitPrebuiltLiveStreamingFragment::class.java
    
    val output = StringBuilder()
    
    output.append("=== CONFIG FIELDS ===").append("\n")
    configClass.fields.forEach { 
        output.append(it.name).append(" : ").append(it.type.simpleName).append("\n")
    }
    
    output.append("\n=== FRAGMENT METHODS ===").append("\n")
    fragmentClass.declaredMethods.filter { it.name.startsWith("new") || it.name.contains("token", ignoreCase=true) }.forEach { 
        output.append(it.name).append("(").append(it.parameterTypes.map { p -> p.simpleName }.joinToString(", ")).append(")\n")
    }
    
    File("zego_reflection.txt").writeText(output.toString())
}
