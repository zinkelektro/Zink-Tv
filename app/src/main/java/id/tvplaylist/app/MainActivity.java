package id.tvplaylist.app;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.text.*;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.MimeTypes;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;
import org.json.*;
import java.io.*;
import java.util.*;

public class MainActivity extends Activity {
    private final String[] files={"playlist1", "playlistdp", "test_playlist"};
    private final ArrayList<Channel> all=new ArrayList<>(), visible=new ArrayList<>();
    private final ArrayList<String> groups=new ArrayList<>();
    private Spinner sources, categories; private EditText search; private ListView list;
    private TextView count, status; private LinearLayout root; private ArrayAdapter<String> adapter;
    private ExoPlayer player; private PlayerView playerView; private boolean watching=false;
    private int sourceIndex=0, groupIndex=0, selected=0; private String query="";
    static class Channel { String name, group, url; Channel(JSONObject o) throws JSONException {name=o.getString("name");group=o.getString("group");url=o.getString("url");} }
    int dp(int n){return (int)(getResources().getDisplayMetrics().density*n+0.5f);}
    TextView text(String s,int size){ TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(Color.WHITE);t.setPadding(dp(12),dp(8),dp(12),dp(8));return t; }
    LinearLayout page(){LinearLayout v=new LinearLayout(this);v.setOrientation(LinearLayout.VERTICAL);v.setBackgroundColor(Color.rgb(16,26,40));v.setPadding(dp(12),dp(12),dp(12),dp(12));setContentView(v);return v;}
    void addHeader(LinearLayout parent){ImageView banner=new ImageView(this);banner.setImageResource(R.drawable.welcome_banner);banner.setAdjustViewBounds(true);banner.setScaleType(ImageView.ScaleType.FIT_CENTER);parent.addView(banner,new LinearLayout.LayoutParams(-1,-2));}
    @Override public void onCreate(Bundle b){super.onCreate(b);showCatalog();}
    ArrayAdapter<String> choices(List<String> items){ArrayAdapter<String> a=new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,items);a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);return a;}
    void showCatalog(){
        watching=false; root=page(); addHeader(root); root.addView(text("TV PLAYLIST",26));
        sources=new Spinner(this);sources.setAdapter(choices(Arrays.asList(files)));sources.setSelection(sourceIndex);root.addView(sources);
        categories=new Spinner(this);root.addView(categories);
        search=new EditText(this);search.setSingleLine(true);search.setHint("Cari nama channel");search.setText(query);root.addView(search);
        count=text("",14);root.addView(count);list=new ListView(this);list.setFocusable(true);root.addView(list,new LinearLayout.LayoutParams(-1,0,1));
        adapter=new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,new ArrayList<>());list.setAdapter(adapter);
        load(sourceIndex,groupIndex);
        sources.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){public void onNothingSelected(AdapterView<?> p){} public void onItemSelected(AdapterView<?> p,View v,int i,long id){if(i!=sourceIndex){sourceIndex=i;groupIndex=0;selected=0;load(i,0);}}});
        categories.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){public void onNothingSelected(AdapterView<?> p){} public void onItemSelected(AdapterView<?> p,View v,int i,long id){groupIndex=i;filter();}});
        search.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int a,int c,int f){} public void onTextChanged(CharSequence s,int a,int b,int c){query=s.toString();selected=0;filter();}public void afterTextChanged(Editable e){}});
        list.setOnItemClickListener((p,v,i,id)->{selected=i;play(visible.get(i));}); list.requestFocus();list.setSelection(selected);
    }
    void load(int i,int group){all.clear();groups.clear();groups.add("Semua kategori");try(InputStream in=getAssets().open(files[i]+".json"); ByteArrayOutputStream out=new ByteArrayOutputStream()){
        byte[] buf=new byte[8192];int n;while((n=in.read(buf))!=-1)out.write(buf,0,n);JSONArray a=new JSONArray(out.toString("UTF-8"));TreeSet<String> set=new TreeSet<>(String.CASE_INSENSITIVE_ORDER);for(int j=0;j<a.length();j++){Channel c=new Channel(a.getJSONObject(j));all.add(c);set.add(c.group);}groups.addAll(set);
    }catch(Exception e){Toast.makeText(this,"Playlist gagal dibaca",Toast.LENGTH_LONG).show();}
    groupIndex=Math.min(group,groups.size()-1);categories.setAdapter(choices(groups));categories.setSelection(groupIndex);filter();}
    void filter(){if(adapter==null)return;visible.clear();adapter.clear();String q=query.toLowerCase(Locale.ROOT);String g=groups.isEmpty()?"":groups.get(Math.min(groupIndex,groups.size()-1));for(Channel c:all){if((groupIndex==0||c.group.equalsIgnoreCase(g))&&c.name.toLowerCase(Locale.ROOT).contains(q)){visible.add(c);adapter.add(c.name+"  ·  "+c.group);}}count.setText(visible.size()+" channel");adapter.notifyDataSetChanged();}
    void play(Channel c){watching=true;root=page();Button back=new Button(this);back.setText("Kembali ke daftar");back.setOnClickListener(v->leavePlayer());root.addView(back);root.addView(text(c.name,20));status=text("Memuat siaran…",14);root.addView(status);playerView=new PlayerView(this);root.addView(playerView,new LinearLayout.LayoutParams(-1,0,1));playerView.setKeepScreenOn(true);player=new ExoPlayer.Builder(this).build();playerView.setPlayer(player);player.addListener(new Player.Listener(){public void onPlaybackStateChanged(int state){if(state==Player.STATE_READY)status.setText("");else if(state==Player.STATE_BUFFERING)status.setText("Memuat siaran…");else if(state==Player.STATE_ENDED)status.setText("Siaran selesai");}public void onPlayerError(PlaybackException e){status.setText("Siaran gagal diputar ("+e.getErrorCodeName()+"). Coba channel lain.");}});
        MediaItem.Builder item=new MediaItem.Builder().setUri(c.url);String path=android.net.Uri.parse(c.url).getPath();if(path!=null){path=path.toLowerCase(Locale.ROOT);if(path.endsWith(".m3u8"))item.setMimeType(MimeTypes.APPLICATION_M3U8);else if(path.endsWith(".mpd"))item.setMimeType(MimeTypes.APPLICATION_MPD);}player.setMediaItem(item.build());player.prepare();player.play();back.requestFocus();}
    void release(){if(player!=null){if(playerView!=null)playerView.setPlayer(null);player.release();player=null;}}
    void leavePlayer(){release();showCatalog();}
    @Override public void onBackPressed(){if(watching)leavePlayer();else super.onBackPressed();}
    @Override protected void onStop(){super.onStop();if(player!=null)player.pause();}
    @Override protected void onDestroy(){release();super.onDestroy();}
}
