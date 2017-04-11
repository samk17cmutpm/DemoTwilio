package com.neo_lab.demotwilio.ui.chatting;


import android.app.Activity;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.neo_lab.demotwilio.R;
import com.neo_lab.demotwilio.model.Token;
import com.neo_lab.demotwilio.share_preferences_manager.SharedPreferencesManager;
import com.neo_lab.demotwilio.utils.toolbar.ToolbarUtils;
import com.twilio.chat.CallbackListener;
import com.twilio.chat.Channel;
import com.twilio.chat.ChannelListener;
import com.twilio.chat.ChatClient;
import com.twilio.chat.ErrorInfo;
import com.twilio.chat.Member;
import com.twilio.chat.Message;
import com.twilio.chat.StatusListener;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;


public class ChattingFragment extends Fragment implements ChattingContract.View {

    private ChattingContract.Presenter presenter;

    private View root;

    private Activity activity;

    @BindView(R.id.toolbar) Toolbar toolbar;

    @BindView(R.id.toolbar_title) TextView tvToolbarTitle;

    @BindView(R.id.messagesRecyclerView) RecyclerView messagesRecyclerView;

    private MessagesAdapter messagesAdapter;

    @BindView(R.id.writeMessageEditText) EditText writeMessageEditText;

    @BindView(R.id.sendChatMessageButton) Button sendChatMessageButton;

    private ArrayList<Message> messages;

    private ChatClient mChatClient;

    private Channel mGeneralChannel;

    private static final String TAG = ChattingFragment.class.getName();

    public ChattingFragment() {
        // Required empty public constructor
    }

    public static ChattingFragment newInstance() {
        ChattingFragment fragment = new ChattingFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_chatting, container, false);

        ButterKnife.bind(this, root);

        showUI();


        // Get Device Id
        String deviceId = Settings.Secure.getString(activity.getContentResolver(), Settings.Secure.ANDROID_ID);

        // Request Token From Server
        presenter.requestToken(deviceId, "Nguyen_Van_Sam");

        return root;
    }

    @Override
    public void setPresenter(ChattingContract.Presenter presenter) {

        this.presenter = presenter;

    }

    @Override
    public void showUI() {

        activity = getActivity();

        ToolbarUtils.initialize(toolbar, activity, R.string.message_general_loading, R.drawable.ic_cancel);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.finish();
                activity.onBackPressed();
            }
        });

        messages = new ArrayList<>();

        LinearLayoutManager layoutManager = new LinearLayoutManager(activity);
        // for a chat app, show latest at the bottom
        layoutManager.setStackFromEnd(true);

        messagesRecyclerView.setLayoutManager(layoutManager);

        messagesAdapter = new MessagesAdapter();
        messagesRecyclerView.setAdapter(messagesAdapter);

        sendChatMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mGeneralChannel != null) {
                    String messageBody = writeMessageEditText.getText().toString();
                    Message message = mGeneralChannel.getMessages().createMessage(messageBody);
                    Log.d(TAG,"Message created");
                    mGeneralChannel.getMessages().sendMessage(message, new StatusListener() {
                        @Override
                        public void onSuccess() {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    // need to modify user interface elements on the UI thread
                                    writeMessageEditText.setText("");
                                }
                            });

                        }

                        @Override
                        public void onError(ErrorInfo errorInfo) {
                            Log.e(TAG,"Error sending message: " + errorInfo.getErrorText());
                        }
                    });
                }
            }
        });

    }

    @Override
    public void onListenerRequestToken(boolean status, String message, Token token) {

        updateStatusRequestToken(status, message);

        if (status) {
            createChattingRom(token.getToken());
        }

    }

    @Override
    public void updateStatusRequestToken(boolean status, String message) {
        int color;
        if (status) {
            color = getResources().getColor(R.color.white);
        } else {
            color = getResources().getColor(R.color.colorAccent);
        }

        tvToolbarTitle.setText(message);
        tvToolbarTitle.setTextColor(color);
    }

    @Override
    public void createChattingRom(String accessToken) {

        ChatClient.Properties.Builder builder = new ChatClient.Properties.Builder();
        builder.setSynchronizationStrategy(ChatClient.SynchronizationStrategy.ALL);
        ChatClient.Properties props = builder.createProperties();
        ChatClient.create(activity, accessToken, props, mChatClientCallback);

    }

    private void loadChannels() {
        final String nameRoomChat = SharedPreferencesManager.getInstance(activity).getString(SharedPreferencesManager.Key.NAME_OF_ROOM_CHAT);
        mChatClient.getChannels().getChannel(nameRoomChat, new CallbackListener<Channel>() {
            @Override
            public void onSuccess(Channel channel) {
                if (channel != null) {
                    joinChannel(channel);
                } else {
                    mChatClient.getChannels().createChannel(nameRoomChat,
                            Channel.ChannelType.PUBLIC, new CallbackListener<Channel>() {
                                @Override
                                public void onSuccess(Channel channel) {
                                    if (channel != null) {
                                        joinChannel(channel);
                                    }
                                }

                                @Override
                                public void onError(ErrorInfo errorInfo) {
                                    Log.e(TAG,"Error creating channel: " + errorInfo.getErrorText());
                                }
                            });
                }
            }

            @Override
            public void onError(ErrorInfo errorInfo) {
                Log.e(TAG,"Error retrieving channel: " + errorInfo.getErrorText());
            }

        });

    }

    private void joinChannel(final Channel channel) {
        Log.d(TAG, "Joining Channel: " + channel.getUniqueName());
        channel.join(new StatusListener() {
            @Override
            public void onSuccess() {
                mGeneralChannel = channel;
                Log.d(TAG, "Joined default channel");
                mGeneralChannel.addListener(mDefaultChannelListener);
            }

            @Override
            public void onError(ErrorInfo errorInfo) {
                Log.e(TAG,"Error joining channel: " + errorInfo.getErrorText());
            }
        });
    }

    private CallbackListener<ChatClient> mChatClientCallback =
            new CallbackListener<ChatClient>() {
                @Override
                public void onSuccess(ChatClient chatClient) {
                    mChatClient = chatClient;
                    loadChannels();
                    Log.d(TAG, "Success creating Twilio Chat Client");
                }

                @Override
                public void onError(ErrorInfo errorInfo) {
                    Log.e(TAG,"Error creating Twilio Chat Client: " + errorInfo.getErrorText());
                }
            };

    private ChannelListener mDefaultChannelListener = new ChannelListener() {
        @Override
        public void onMessageAdd(final Message message) {
            Log.d(TAG, "Message added");
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // need to modify user interface elements on the UI thread
                    messages.add(message);
                    messagesAdapter.notifyDataSetChanged();
                }
            });

        }

        @Override
        public void onMessageChange(Message message) {
            Log.d(TAG, "Message changed: " + message.getMessageBody());
        }

        @Override
        public void onMessageDelete(Message message) {
            Log.d(TAG, "Message deleted");
        }

        @Override
        public void onMemberJoin(Member member) {
            Log.d(TAG, "Member joined: " + member.getUserInfo().getIdentity());
        }

        @Override
        public void onMemberChange(Member member) {
            Log.d(TAG, "Member changed: " + member.getUserInfo().getIdentity());
        }

        @Override
        public void onMemberDelete(Member member) {
            Log.d(TAG, "Member deleted: " + member.getUserInfo().getIdentity());
        }

        @Override
        public void onTypingStarted(Member member) {
            Log.d(TAG, "Started Typing: " + member.getUserInfo().getIdentity());
        }

        @Override
        public void onTypingEnded(Member member) {
            Log.d(TAG, "Ended Typing: " + member.getUserInfo().getIdentity());
        }

        @Override
        public void onSynchronizationChange(Channel channel) {

        }
    };

    class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.ViewHolder> {

        class ViewHolder extends RecyclerView.ViewHolder {

            public TextView mMessageTextView;

            public ViewHolder(TextView textView) {
                super(textView);
                mMessageTextView = textView;
            }
        }

        public MessagesAdapter() {

        }

        @Override
        public MessagesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                               int viewType) {
            TextView messageTextView = (TextView) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.message_chatting_item, parent, false);
            return new ViewHolder(messageTextView);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Message message = messages.get(position);
            String messageText = String.format("%s: %s", message.getAuthor(), message.getMessageBody());
            holder.mMessageTextView.setText(messageText);

        }

        @Override
        public int getItemCount() {
            return messages.size();
        }
    }



}
