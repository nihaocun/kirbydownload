package com.kirby.runanjing.activity;

import android.animation.*;
import android.app.*;
import android.content.*;
import android.graphics.*;
import android.os.*;
import android.support.design.widget.*;
import android.support.v4.widget.*;
import android.support.v7.app.*;
import android.support.v7.widget.*;
import android.util.*;
import android.view.*;
import android.view.inputmethod.*;
import android.widget.*;
import cn.bmob.v3.*;
import cn.bmob.v3.exception.*;
import cn.bmob.v3.listener.*;
import com.kirby.runanjing.*;
import com.kirby.runanjing.adapter.*;
import com.kirby.runanjing.untils.*;
import java.util.*;

import android.app.AlertDialog;
import android.support.v7.widget.Toolbar;

public class MessageActivity extends AppCompatActivity
{
	private SwipeRefreshLayout 刷新;
	private List<Mess> messlist = new ArrayList<>();
	private String name;
	private MessageAdapter adapter;
	private RecyclerView re;
	private String email;
	private String id;

	private FloatingActionButton 编写;

	private RelativeLayout edit;

	private EditText edittext;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.message_layout);
		//配置toolbar
		Toolbar toolbar=(Toolbar)findViewById(R.id.标题栏);
		setSupportActionBar(toolbar);
		//初始化Bmob
		Bmob.initialize(this, "e39c2e15ca40b358b0dcc933236c1165");
		//使用Bmob从服务器获取数据
		getMessage();
	    //设置显示留言的列表
		re = (RecyclerView)findViewById(R.id.留言);
		StaggeredGridLayoutManager stagger=new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
		//GridLayoutManager layoutManager=new GridLayoutManager(this, 1);
		re.setLayoutManager(stagger);
		adapter = new MessageAdapter(messlist);	
		//刷新数据
		刷新 = (SwipeRefreshLayout)findViewById(R.id.刷新);
		刷新.setColorSchemeColors(Color.BLUE, Color.RED, Color.YELLOW);
		刷新.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener(){
				@Override
				public void onRefresh()
				{
					//刷新
					getMessage();
				}
			});
		//使用BmobUser类获取部分用户数据
		MyUser u = BmobUser.getCurrentUser(MyUser.class);
		name = u.getUsername();
		toolbar.setSubtitle(name);
		编写 = (FloatingActionButton)findViewById(R.id.FAB_编辑);
		编写.setOnClickListener(new View.OnClickListener(){
				private boolean 状态_;

				private RelativeLayout edit;
				@Override
				public void onClick(View v)			
				{       
					//处理发送
					SharedPreferences 状态=getSharedPreferences("boolean", Context.MODE_WORLD_READABLE);
					状态_ = 状态.getBoolean("send_状态", false);
					if (状态_ == false)
					{
						openSend();
					}
					else
					{
						closeSend();
					}
					ImageView 发送=(ImageView) findViewById(R.id.发送);
					发送.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v)
							{	
								closeSend();
								//获取字符串转化为string数据
								EditText 内容=(EditText)findViewById(R.id.内容_编辑);
								String edit_内容=内容.getText().toString();
								//判断是否为空
								if (edit_内容.isEmpty())
								{
									Toast.makeText(MessageActivity.this, "内容不能为空！", Toast.LENGTH_SHORT).show();
								}
								else
								{
									//自定义MessBmob发送留言
									MessageBmob mess = new MessageBmob();
									mess.setMessage(edit_内容);
									mess.setNickname(name);
									mess.save(new SaveListener<String>() {
											@Override
											public void done(String objectId, BmobException e)
											{
												if (e == null)
												{			
													getMessage();
													Toast.makeText(MessageActivity.this, "发送成功：" + objectId, Toast.LENGTH_SHORT).show();
												}
												else
												{
													Toast.makeText(MessageActivity.this, "发送失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
												}
											}
										});
								}
							}
						});
				}
			});
	}
	private void openSend()
	{
	    edit = (RelativeLayout)findViewById(R.id.edit);
		ObjectAnimator animator = ObjectAnimator.ofFloat(edit, "alpha", 0f, 1f);
		animator.setDuration(300);//时间0.5s
		edit.setVisibility(View.VISIBLE);
		ObjectAnimator animator2 = ObjectAnimator.ofFloat(编写, "rotation", 0f, 135f);
		animator2.setDuration(300);
		AnimatorSet set = new AnimatorSet();
		set.play(animator).with(animator2);
		set.start();
		SharedPreferences.Editor y=getSharedPreferences("boolean", MODE_PRIVATE).edit().putBoolean("send_状态", true);
		y.apply();
		edittext = (EditText)findViewById(R.id.内容_编辑);
	    edittext.setFocusable(true);  
		edittext.setFocusableInTouchMode(true);  
		edittext.requestFocus();  
		InputMethodManager inputManager =  
			(InputMethodManager)edittext.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);  
		inputManager.showSoftInput(edittext, 0);  
	}
	private void closeSend()
	{
		ObjectAnimator animator = ObjectAnimator.ofFloat(edit, "alpha", 1f, 0f);
		animator.setDuration(300);//时间0.5s
		edit.setVisibility(View.GONE);
		ObjectAnimator animator2 = ObjectAnimator.ofFloat(编写, "rotation", 135f, 0f);
		animator2.setDuration(300);
		AnimatorSet set = new AnimatorSet();
		set.play(animator).with(animator2);
		set.start();
		SharedPreferences.Editor y=getSharedPreferences("boolean", MODE_PRIVATE).edit().putBoolean("send_状态", false);
		y.apply();
		InputMethodManager inputManager =  
			(InputMethodManager)edittext.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);  
		inputManager.hideSoftInputFromInputMethod(edittext.getWindowToken(), 0);
	}		
	private void getMessage()
	{
		messlist.clear();//清空列表
		//使用BmobQuery获取留言数据
		BmobQuery<MessageBmob> query=new BmobQuery<MessageBmob>();
		query.order("-createdAt");//时间降序排列
		query.findObjects(new FindListener<MessageBmob>() {
				@Override
				public void done(List<MessageBmob> list, BmobException e)
				{
					if (e == null)
					{
						Message message = handler.obtainMessage();
						message.what = 0;
						//以消息为载体
						message.obj = list;//这里的list就是查询出list
						//向handler发送消息
						handler.sendMessage(message);
					}
					else
					{
						Log.e("bmob", "" + e);
					}
				}
			});
	}
	private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg)
		{
            switch (msg.what)
			{
                case 0:
                    List<MessageBmob> list= (List<MessageBmob>)msg.obj;
					for (MessageBmob m : list)
					{
						//从获取的数据中提取需要的数据
						String 用户名=m.getNickname();
						String 内容=m.getMessage();
						String 时间=m.getCreatedAt();
						Mess mess=new Mess(用户名, 内容, 时间);
						//将查询到的数据依次添加到列表
						messlist.add(mess);
						//设置适配器
						re.setAdapter(adapter);
					}			
					//刷新回调
					刷新.setRefreshing(false);
                    break;
            }
        }
    };
	@Override
	protected void onDestroy()//在退出程序时恢复数据
	{	
		super.onDestroy();
		SharedPreferences.Editor y=getSharedPreferences("boolean", MODE_PRIVATE).edit().putBoolean("send_状态", false);
		y.apply();
    }
	public boolean onCreateOptionsMenu(Menu menu)
	{
        getMenuInflater().inflate(R.menu.toolbar2, menu);
        return true;
    }
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.account:
				final MyUser u = BmobUser.getCurrentUser(MyUser.class);
				name = u.getUsername();
				email = u.getEmail();
				id = u.getObjectId();
				new AlertDialog.Builder(MessageActivity.this)
					.setTitle("用户信息")
					.setMessage("用户名:" + name + "\n邮箱:" + email)
					.setPositiveButton("修改邮箱", new
					DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							LayoutInflater lay_1 = getLayoutInflater();
							final View modification_email_layout = lay_1.inflate(R.layout.dialog_modification_email, null);
							new AlertDialog.Builder(MessageActivity.this)
								.setTitle("修改邮箱(成功后需要重新登录)")
								.setView(modification_email_layout) 
								.setPositiveButton("确定", new
								DialogInterface.OnClickListener()
								{
									@Override
									public void onClick(DialogInterface dialog, int which)
									{
										EditText 修改邮箱_原邮箱=(EditText)modification_email_layout.findViewById(R.id.修改邮箱_原邮箱);
										EditText 修改邮箱_新邮箱=(EditText)modification_email_layout.findViewById(R.id.修改邮箱_新邮箱);
										String edit_原邮箱=修改邮箱_原邮箱.getText().toString();
										String edit_新邮箱=修改邮箱_新邮箱.getText().toString();
										if (edit_原邮箱.isEmpty() || edit_新邮箱.isEmpty())
										{
											Toast.makeText(MessageActivity.this, "不能为空！", Toast.LENGTH_SHORT).show();
										}
										else
										{
											if (email.equals(edit_原邮箱))
											{
												MyUser 邮箱=new MyUser();
												邮箱.setEmail(edit_新邮箱);
												邮箱.update(id, new UpdateListener() {

														@Override
														public void done(BmobException e)
														{
															if (e == null)
															{
																Toast.makeText(MessageActivity.this, "修改成功", Toast.LENGTH_SHORT).show();
																u.logOut();
																Intent 修改邮箱=new Intent(MessageActivity.this, LoginActivity.class);
																startActivity(修改邮箱);
															}
															else
															{
																Toast.makeText(MessageActivity.this, "修改失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
															}
														}

													});
											}
											else
											{
												Toast.makeText(MessageActivity.this, "原邮箱出错", Toast.LENGTH_SHORT).show();
											}
										}
									}
								}
							)					
								.setNegativeButton("取消", null)
								.show();
						}
					}
				)
					.setNegativeButton("修改密码", new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							LayoutInflater lay_2 = getLayoutInflater();
							final View modification_password_layout = lay_2.inflate(R.layout.dialog_modification_password, null);
							new AlertDialog.Builder(MessageActivity.this)
								.setTitle("修改密码(成功后需要重新登录)")
								.setView(modification_password_layout) 
								.setPositiveButton("确定", new
								DialogInterface.OnClickListener()
								{

									private int text;
									@Override
									public void onClick(DialogInterface dialog, int which)
									{
										EditText 修改密码_原密码=(EditText)modification_password_layout.findViewById(R.id.修改密码_原密码);
										EditText 修改密码_新密码=(EditText)modification_password_layout.findViewById(R.id.修改密码_新密码);
										EditText 修改密码_验证=(EditText)modification_password_layout.findViewById(R.id.修改密码_验证);
										String edit_原密码=修改密码_原密码.getText().toString();
										String edit_新密码=修改密码_新密码.getText().toString();
										String edit_验证=修改密码_验证.getText().toString();
										if (edit_原密码.isEmpty() || edit_新密码.isEmpty() || edit_验证.isEmpty())
										{
											Toast.makeText(MessageActivity.this, "不能为空！", Toast.LENGTH_SHORT).show();
										}
										else
										{
											if (edit_新密码.equals(edit_验证))
											{
												final MyUser pas = new MyUser();
												pas.updateCurrentUserPassword(edit_原密码, edit_新密码, new UpdateListener(){
														@Override
														public void done(BmobException e)
														{
															if (e == null)
															{
																Toast.makeText(MessageActivity.this, "修改成功", Toast.LENGTH_SHORT).show();
																u.logOut();
																Intent 修改密码=new Intent(MessageActivity.this, LoginActivity.class);
																startActivity(修改密码);
															}
															else
															{
																Toast.makeText(MessageActivity.this, "修改失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
															}
														}
													});
											}
											else
											{
												Toast.makeText(MessageActivity.this, "两次输入的新密码不同", Toast.LENGTH_SHORT).show();
											}
										}
									}
								}
							)					
								.setNegativeButton("取消", null)
								.show();
						}
					}
				)
					.setNeutralButton("账号登出", new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							u.logOut();
							Toast.makeText(MessageActivity.this, "登出成功", Toast.LENGTH_SHORT).show();
							Intent 登出=new Intent(MessageActivity.this, MainActivity.class);
							startActivity(登出);
						}
					}
				).show();
				break;
			default:
		}
		return true;
	}
}
