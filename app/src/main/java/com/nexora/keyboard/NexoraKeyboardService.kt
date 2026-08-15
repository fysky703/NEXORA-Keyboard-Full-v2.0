package com.nexora.keyboard
import android.app.AlertDialog
import android.content.*
import android.graphics.Color
import android.inputmethodservice.InputMethodService
import android.view.*
import android.widget.*
import kotlinx.coroutines.*

class NexoraKeyboardService:InputMethodService(){
 private var lang="EN"; private lateinit var root:LinearLayout; private lateinit var keys:LinearLayout; private lateinit var langBtn:TextView
 private val scope=CoroutineScope(SupervisorJob()+Dispatchers.Main.immediate)
 private fun key(s:String)=TextView(this).apply{text=s;textSize=19f;gravity=Gravity.CENTER;setTextColor(Color.WHITE);setBackgroundColor(Color.rgb(35,36,40))}
 override fun onCreateInputView():View{
  root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(6,6,6,6);setBackgroundColor(Color.rgb(8,9,12))}
  val bar=LinearLayout(this)
  val clip=key("📋").apply{setOnClickListener{showClips()}}
  val files=key("📁").apply{setOnClickListener{Toast.makeText(this@NexoraKeyboardService,"Open NEXORA app for Files",Toast.LENGTH_SHORT).show()}}
  val emoji=key("😊").apply{setOnClickListener{currentInputConnection?.commitText("😊",1)}}
  langBtn=key("EN").apply{setOnClickListener{lang=if(lang=="EN")"KM" else "EN";text=lang;render()}}
  listOf(clip,files,emoji,langBtn).forEach{bar.addView(it,LinearLayout.LayoutParams(0,50,1f))}
  root.addView(bar);keys=LinearLayout(this);keys.orientation=LinearLayout.VERTICAL;root.addView(keys,LinearLayout.LayoutParams(-1,0,1f));render();return root
 }
 private fun render(){
  keys.removeAllViews()
  val rows=if(lang=="EN") listOf("1234567890".map{it.toString()},"qwertyuiop".map{it.toString()},"asdfghjkl".map{it.toString()},listOf("⇧","z","x","c","v","b","n","m","⌫"),listOf("🌐","😊",",","SPACE",".","↵")) else KhmerLayout.rows+listOf(listOf("🌐","😊","ា","SPACE","។","⌫"))
  rows.forEach{r->val line=LinearLayout(this);r.forEach{s->val v=key(s);v.setOnClickListener{press(s)};line.addView(v,LinearLayout.LayoutParams(0,0,1f))};keys.addView(line,LinearLayout.LayoutParams(-1,0,1f))}
 }
 private fun press(s:String){val ic=currentInputConnection?:return;when(s){"SPACE"->ic.commitText(" ",1);"⌫"->ic.deleteSurroundingText(1,0);"↵"->ic.sendKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN,android.view.KeyEvent.KEYCODE_ENTER);"🌐"->{lang=if(lang=="EN")"KM" else "EN";langBtn.text=lang;render()};"😊"->ic.commitText("😊",1);"⇧"->{}else->ic.commitText(s,1)}}
 private fun showClips(){scope.launch{val l=NexoraDb.get(this@NexoraKeyboardService).clipDao().all().take(80);if(l.isEmpty()){Toast.makeText(this@NexoraKeyboardService,"Clipboard Library is empty",Toast.LENGTH_SHORT).show();return@launch};AlertDialog.Builder(this@NexoraKeyboardService).setTitle("NEXORA Clipboard").setItems(l.map{(if(it.pinned)"📌 " else "")+it.text.replace("\n"," ").take(70)}.toTypedArray()){_,i->currentInputConnection?.commitText(l[i].text,1)}.setNegativeButton("Close",null).show()}}
 override fun onDestroy(){scope.cancel();super.onDestroy()}
}
