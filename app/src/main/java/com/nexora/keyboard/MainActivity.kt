package com.nexora.keyboard
import android.app.*
import android.os.*
import android.content.*
import android.graphics.Color
import android.net.Uri
import android.provider.Settings
import android.view.*
import android.widget.*
import kotlinx.coroutines.*
import java.io.OutputStreamWriter

class MainActivity:Activity(){
 private lateinit var list:LinearLayout
 private val scope=CoroutineScope(SupervisorJob()+Dispatchers.Main.immediate)
 private val db by lazy{NexoraDb.get(this)}
 override fun onCreate(b:Bundle?){super.onCreate(b);build()}
 private fun build(){
  val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(18,24,18,18);setBackgroundColor(Color.rgb(8,9,12))}
  root.addView(TextView(this).apply{text="NEXORA Keyboard";textSize=28f;setTextColor(Color.WHITE);gravity=Gravity.CENTER},LinearLayout.LayoutParams(-1,70))
  root.addView(Button(this).apply{text="Enable / Select Keyboard";setOnClickListener{startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))}})
  root.addView(Button(this).apply{text="+ New Clipboard";setOnClickListener{newClip()}})
  root.addView(Button(this).apply{text="📁 Create File / Choose Location";setOnClickListener{createFile()}})
  val search=EditText(this).apply{hint="Search Clipboard…";setTextColor(Color.WHITE);setHintTextColor(Color.GRAY)}
  root.addView(search);list=LinearLayout(this);list.orientation=LinearLayout.VERTICAL;val scroll=ScrollView(this);scroll.addView(list);root.addView(scroll,LinearLayout.LayoutParams(-1,0,1f))
  setContentView(root);refresh("");search.setOnEditorActionListener{_,_,_->refresh(search.text.toString());false}
 }
 private fun refresh(q:String){scope.launch{val cs=if(q.isBlank())db.clipDao().all() else db.clipDao().search(q);list.removeAllViews();cs.forEach{c->val r=LinearLayout(this@MainActivity);r.orientation=LinearLayout.VERTICAL;val t=TextView(this@MainActivity);t.text=(if(c.pinned)"📌 " else "")+(if(c.favorite)"⭐ " else "")+c.text;t.setTextColor(Color.WHITE);t.textSize=16f;t.setOnClickListener{copy(c.text)};r.addView(t);val a=LinearLayout(this@MainActivity);val p=Button(this@MainActivity);p.text=if(c.pinned)"Unpin" else "Pin";p.setOnClickListener{scope.launch{db.clipDao().update(c.copy(pinned=!c.pinned));refresh(q)}};val f=Button(this@MainActivity);f.text=if(c.favorite)"★" else "☆";f.setOnClickListener{scope.launch{db.clipDao().update(c.copy(favorite=!c.favorite));refresh(q)}};val d=Button(this@MainActivity);d.text="Delete";d.setOnClickListener{scope.launch{db.clipDao().delete(c);refresh(q)}};listOf(p,f,d).forEach{a.addView(it,LinearLayout.LayoutParams(0,-2,1f))};r.addView(a);list.addView(r)}}}
 private fun newClip(){val e=EditText(this);e.setTextColor(Color.WHITE);e.hint="Paste/type text";AlertDialog.Builder(this).setTitle("New Clipboard").setView(e).setPositiveButton("Save"){_,_->scope.launch{db.clipDao().insert(Clip(text=e.text.toString()));refresh("")}}.setNegativeButton("Cancel",null).show()}
 private fun copy(s:String){(getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager).setPrimaryClip(android.content.ClipData.newPlainText("NEXORA",s));Toast.makeText(this,"Copied",Toast.LENGTH_SHORT).show()}
 private fun createFile(){val e=EditText(this);e.hint="file.txt";e.setTextColor(Color.WHITE);AlertDialog.Builder(this).setTitle("Create text file").setView(e).setPositiveButton("Choose location"){_,_->startActivityForResult(Intent(Intent.ACTION_CREATE_DOCUMENT).apply{type="text/plain";putExtra(Intent.EXTRA_TITLE,e.text.toString().ifBlank{"nexora.txt"})},901)}.setNegativeButton("Cancel",null).show()}
 override fun onActivityResult(r:Int,c:Int,d:Intent?){super.onActivityResult(r,c,d);if(r==901&&c==RESULT_OK){val u:Uri=d?.data?:return;scope.launch{val t=db.clipDao().all().firstOrNull()?.text?:"";contentResolver.openOutputStream(u)?.use{OutputStreamWriter(it).use{w->w.write(t)}};Toast.makeText(this@MainActivity,"Saved",Toast.LENGTH_SHORT).show()}}}
 override fun onDestroy(){scope.cancel();super.onDestroy()}
}
