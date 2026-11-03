package com.unseretage.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.unseretage.app.databinding.ActivityLegalBinding

/**
 * Zeigt den KI-Hinweis, Haftung, Datenschutz und die Lizenzen.
 * Die App ist oeffentlich herunterladbar, also soll jeder nachlesen koennen,
 * wie sie entstanden ist.
 */
class LegalActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLegalBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLegalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvVersionLine.text =
            "Version ${Updater.installedName(this)} (Build ${Updater.installedBuild(this)})"

        binding.btnProject.setOnClickListener {
            open("https://github.com/${Updater.REPO}")
        }
        binding.btnApache.setOnClickListener {
            open("https://www.apache.org/licenses/LICENSE-2.0")
        }
        binding.btnThirdParty.setOnClickListener {
            open("https://github.com/${Updater.REPO}/blob/main/THIRD-PARTY-LICENSES.md")
        }
        binding.btnClose.setOnClickListener { finish() }
    }

    private fun open(url: String) {
        runCatching {
            startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }
}
