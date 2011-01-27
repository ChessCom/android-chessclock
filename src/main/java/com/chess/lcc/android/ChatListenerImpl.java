package com.chess.lcc.android;

import com.chess.live.client.*;
import com.chess.live.client.impl.util.DateTimeUtils;

import java.util.Collection;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Vova
 * Date: 28.02.2010
 * Time: 18:33:30
 * To change this template use File | Settings | File Templates.
 */

public class ChatListenerImpl implements ChatListener {
    private LccHolder _lccHolder;

    public ChatListenerImpl(LccHolder lccHolder) {
        _lccHolder = lccHolder;
    }

    public void onPublicChatListReceived(Collection<? extends Chat> chats) {
        String str = "Public Chat List received: user=" + _lccHolder.getUser().getUsername() + ", listSize=" + chats.size();
        for (Chat chat : chats) {
            str += "\n  " + "Chat: roomId=" + chat.getId() + ", name=\"" + chat.getName() + "\"";
            if (chat.getId().equals("R1")) {
                _lccHolder.setMainChat(chat);
                str += " - entering...";
                break;
            }
        }
        LccHolder.LOG.info(str);
        if (_lccHolder.getMainChat() != null)
            _lccHolder.getClient().enterChat(_lccHolder.getMainChat(), this);
        else
            LccHolder.LOG.warn("There is no R1 chat in the list of the public chats");
    }

    public void onSubscribedChatListReceived(Collection<? extends Chat> chats) {
        String str = "Previously Subscribed Chat List received: user=" + _lccHolder.getUser().getUsername() + ", listSize=" + chats.size();
        for (Chat chat : chats) {
            str += "\n\t" + "Chat: roomId=" + chat.getId() + ",\tname=" + chat.getName();
        }
        LccHolder.LOG.info(str);
    }

    public void onChatOpened(Chat chat) {
        LccHolder.LOG.info("Chat opened: user=" + _lccHolder.getUser().getUsername() + ", roomId=" + chat.getId() + ", name=\"" + chat.getName() + "\"");
    }

    public void onChatEntered(Chat chat, ChatMember member) {
        LccHolder.LOG.info("Chat entered: roomId=" + chat.getId() + ", enteredUser=" + member + ", thisUser=" + _lccHolder
          .getUser().getUsername());
    }

    public void onChatExited(Chat chat, ChatMember member) {
    }

    public void onChatDisabled(Chat chat, ChatMember member) {
    }

    public void onMembersListReceived(Chat chat, Integer membersCount, Collection<ChatMember> members, ChatMember headMember) {
        String str = "Chat Member List received: roomId=" + chat.getId() + ", user=" + _lccHolder.getUser().getUsername();
        for (ChatMember member : members) {
            str += "\n\tMember: " + member;
        }
        str += (headMember != null ? ("\n\tHead: " + headMember) : "");
        LccHolder.LOG.info(str);
    }

    public void onMessageReceived(Chat chat, ChatMessage message) {
    }

    public void onMessageHistoryReceived(Chat chat, Collection<ChatMessage> messages) {
        String str = "Chat Message History received: roomId=" + chat.getId() + ", user=" + _lccHolder.getUser().getUsername() + ", messagesCount=" + messages.size();
        for (ChatMessage message : messages) {
            str += "\n\t" + "Message: " + DateTimeUtils.fromDateTime(message.getDateTime()) + ",\tauthor=" + message.getAuthor().getUsername() + ",\ttext=\"" + message.getMessage() + "\"";
        }
        LccHolder.LOG.info(str);
    }

    public void onInvitedToPrivateChat(Chat chat, User by, User invited, Collection<ChatMember> members, ChatMember headMember) {
    }

    public void onPrivateChatInvitationCancelled(Chat chat, User by) {
    }

    public void onPrivateChatInvitationAccepted(Chat chat, User by) {
    }

    public void onPrivateChatInvitationRejected(Chat chat, User by) {
    }

    public void onRemovedFromPrivateChat(Chat chat, User by) {
    }

    public void onVoiceKeyReceived(Chat chat, VoiceRole role, String key) {
    }

    public void onPublicChatInfoReceived(Map<String, Integer> info) {
    }

  public void onMessageRemoved(Chat arg0, User arg1, Long arg2) {
        // TODO Auto-generated method stub
    }
}
