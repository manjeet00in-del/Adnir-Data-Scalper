package in.adnir.data;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebResourceRequest;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.net.URLEncoder;
import java.util.HashSet;

public class MainActivity extends Activity {
  private static final int SAVE_CSV=80;
  private WebView web;
  private EditText query,row,name,phone,address;
  private TextView status,results;
  private JSONArray saved=new JSONArray(), preview=new JSONArray();
  private String scanner;
  private String pendingCsv="";
  private int dp(float n){return (int)(getResources().getDisplayMetrics().density*n+.5f);}
  private EditText input(String hint){EditText e=new EditText(this);e.setSingleLine(true);e.setTextSize(13);e.setHint(hint);e.setPadding(dp(8),dp(8),dp(8),dp(8));return e;}
  private Button button(String title){Button b=new Button(this);b.setText(title);b.setAllCaps(false);return b;}
  private void info(String s){status.setText(s);}
  @Override public void onCreate(Bundle b){super.onCreate(b);
    try(java.io.InputStream in=getAssets().open("scanner.js"); java.io.ByteArrayOutputStream buffer=new java.io.ByteArrayOutputStream()){byte[] data=new byte[4096];int n;while((n=in.read(data))!=-1)buffer.write(data,0,n);scanner=buffer.toString("UTF-8");}catch(Exception e){scanner="";}
    try{saved=new JSONArray(getPreferences(0).getString("rows","[]"));}catch(Exception ignored){}
    LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(Color.WHITE);setContentView(root);root.setOnApplyWindowInsetsListener((v,insets)->{v.setPadding(insets.getSystemWindowInsetLeft(),insets.getSystemWindowInsetTop(),insets.getSystemWindowInsetRight(),insets.getSystemWindowInsetBottom());return insets;});
    TextView title=new TextView(this);title.setText("AdNir Data  •  Browser Scanner");title.setTextSize(19);title.setTextColor(Color.WHITE);title.setBackgroundColor(0xff10345e);title.setPadding(dp(14),dp(12),dp(14),dp(12));root.addView(title);
    LinearLayout top=new LinearLayout(this);root.addView(top);
    query=input("दुकान + शहर लिखें या https:// URL");query.setText("Plywood stores in Lucknow");top.addView(query,new LinearLayout.LayoutParams(0,dp(48),1));Button go=button("खोलें");top.addView(go);go.setOnClickListener(v->open());
    web=new WebView(this);web.getSettings().setJavaScriptEnabled(true);web.getSettings().setDomStorageEnabled(true);web.getSettings().setAllowFileAccess(false);web.getSettings().setAllowContentAccess(false);web.getSettings().setSupportMultipleWindows(false);web.setWebViewClient(new WebViewClient(){@Override public boolean shouldOverrideUrlLoading(WebView v,WebResourceRequest r){String scheme=r.getUrl().getScheme();return !("https".equals(scheme)||"http".equals(scheme));}@Override public void onPageFinished(WebView v,String u){info("पेज खुल गया। दुकानों की सूची दिखे तो Scan दबाएँ।");}});root.addView(web,new LinearLayout.LayoutParams(-1,0,1));
    LinearLayout actions=new LinearLayout(this);root.addView(actions);Button scan=button("Scan");Button add=button("जोड़ें");Button csv=button("CSV");Button clear=button("साफ");for(Button x:new Button[]{scan,add,csv,clear})actions.addView(x,new LinearLayout.LayoutParams(0,dp(48),1));
    scan.setOnClickListener(v->scan());add.setOnClickListener(v->add());csv.setOnClickListener(v->export());clear.setOnClickListener(v->{new android.app.AlertDialog.Builder(this).setMessage("सहेजे गए सभी रिकॉर्ड मिटाएँ?").setNegativeButton("रद्द",null).setPositiveButton("मिटाएँ",(d,w)->{saved=new JSONArray();save();show(saved);info("डेटा साफ हो गया।");}).show();});
    status=new TextView(this);status.setPadding(dp(10),dp(4),dp(10),dp(4));root.addView(status);results=new TextView(this);results.setTextSize(12);results.setPadding(dp(10),dp(4),dp(10),dp(8));ScrollView scroll=new ScrollView(this);scroll.addView(results);root.addView(scroll,new LinearLayout.LayoutParams(-1,dp(100)));
    LinearLayout advanced=new LinearLayout(this);advanced.setOrientation(LinearLayout.VERTICAL);root.addView(advanced);Button toggle=button("अन्य साइट के selectors ▾");advanced.addView(toggle);LinearLayout inputs=new LinearLayout(this);inputs.setOrientation(LinearLayout.VERTICAL);inputs.setVisibility(View.GONE);advanced.addView(inputs);row=input("Listing CSS selector: .card");name=input("नाम: h2");phone=input("फोन: a[href^=tel:]");address=input("पता: .address");for(EditText e:new EditText[]{row,name,phone,address})inputs.addView(e,new LinearLayout.LayoutParams(-1,dp(42)));toggle.setOnClickListener(v->inputs.setVisibility(inputs.getVisibility()==View.GONE?View.VISIBLE:View.GONE));
    info("इंटरनेट ऑन करें, खोजें, फिर Scan दबाएँ।");open();
  }
  private void open(){String s=query.getText().toString().trim();if(s.isEmpty())return;try{String url=s.matches("https?://.*")?s:"https://www.google.com/search?q="+URLEncoder.encode(s,"UTF-8");preview=new JSONArray();web.loadUrl(url);}catch(Exception e){info(e.getMessage());}}
  private void scan(){preview=new JSONArray();if(scanner.isEmpty()){info("Scanner file नहीं मिली।");return;}try{JSONObject cfg=new JSONObject();cfg.put("row",row.getText().toString().trim());cfg.put("name",name.getText().toString().trim());cfg.put("phone",phone.getText().toString().trim());cfg.put("address",address.getText().toString().trim());web.evaluateJavascript("window.__adnirConfig="+cfg.toString()+";try{"+scanner+"}catch(e){JSON.stringify({error:String(e.message||e)})}",raw->{try{Object value=new org.json.JSONTokener(raw).nextValue();String data=String.valueOf(value);if(data.startsWith("{")){info(new JSONObject(data).optString("error","Scan नहीं हुआ"));return;}preview=new JSONArray(data);show(preview);info(preview.length()+" मिले। नाम, फोन, पता जाँचकर जोड़ें दबाएँ।");}catch(Exception e){info("इस पेज पर Scan नहीं हुआ: "+e.getMessage());}});}catch(Exception e){info(e.getMessage());}}
  private void show(JSONArray arr){StringBuilder b=new StringBuilder();for(int i=0;i<Math.min(arr.length(),20);i++){JSONObject x=arr.optJSONObject(i);if(x!=null)b.append(i+1).append(". ").append(x.optString("name")).append(" | ").append(x.optString("phone")).append(" | ").append(x.optString("address")).append('\n');}results.setText(b.toString());}
  private void add(){HashSet<String> seen=new HashSet<>();for(int i=0;i<saved.length();i++){JSONObject x=saved.optJSONObject(i);if(x!=null)seen.add(key(x));}int count=0;for(int i=0;i<preview.length()&&saved.length()<10000;i++){JSONObject x=preview.optJSONObject(i);if(x!=null&&seen.add(key(x))){saved.put(x);count++;}}save();info(count+" नए रिकॉर्ड जोड़े; कुल "+saved.length());show(saved);}
  private String key(JSONObject x){return x.optString("name")+"|"+x.optString("phone")+"|"+x.optString("source");}
  private void save(){getPreferences(0).edit().putString("rows",saved.toString()).apply();}
  private String cell(String s){return "\""+s.replace("\"","\"\"").replace("\r"," ").replace("\n"," ")+"\"";}
  private void export(){if(saved.length()==0){info("पहले Scan और जोड़ें दबाएँ।");return;}StringBuilder b=new StringBuilder("\ufeffName,Phone,Address,Source URL\r\n");for(int i=0;i<saved.length();i++){JSONObject x=saved.optJSONObject(i);if(x!=null)b.append(cell(x.optString("name"))).append(',').append(cell(x.optString("phone"))).append(',').append(cell(x.optString("address"))).append(',').append(cell(x.optString("source"))).append("\r\n");}pendingCsv=b.toString();Intent intent=new Intent(Intent.ACTION_CREATE_DOCUMENT);intent.addCategory(Intent.CATEGORY_OPENABLE);intent.setType("text/csv");intent.putExtra(Intent.EXTRA_TITLE,"AdNir-data.csv");startActivityForResult(intent,SAVE_CSV);}
  @Override protected void onActivityResult(int request,int result,Intent data){super.onActivityResult(request,result,data);if(request==SAVE_CSV&&result==RESULT_OK&&data!=null&&data.getData()!=null){try(OutputStream out=getContentResolver().openOutputStream(data.getData())){out.write(pendingCsv.getBytes(StandardCharsets.UTF_8));info("CSV सेव हो गई।");}catch(Exception e){info("CSV सेव नहीं हुई: "+e.getMessage());}}}
  @Override public void onBackPressed(){if(web.canGoBack())web.goBack();else super.onBackPressed();}
  @Override protected void onDestroy(){web.destroy();super.onDestroy();}
}
