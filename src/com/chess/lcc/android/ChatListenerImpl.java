package com.chess.lcc.android;

import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.IntentConstants;
import com.chess.backend.statics.StaticData;
import com.chess.live.client.*;
import com.chess.live.client.impl.util.DateTimeUtils;
import com.chess.model.GameItem;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class ChatListenerImpl
		implements ChatListener {
	public ChatListenerImpl(LccHolder lccHolder) {
		if (lccHolder == null) {
			throw new NullPointerException(AppConstants.LCC_HOLDER_IS_NULL);
		}
		this.lccHolder = lccHolder;
	}

	@Override
	public void onPublicChatListReceived(Collection<? extends Chat> chats) {
		/*String str = "CHAT LISTENER: Public Chat List received: user=" + lccHolder.getUser().getUsername() + ", listSize=" + chats.size();
			for(Chat chat : chats)
			{
			  str += "\n  " + "Chat: roomId=" + chat.getId() + ", name=\"" + chat.getName() + "\"";
			  if(chat.getId().equals("R1"))
			  {
				lccHolder.setMainChat(chat);
				str += " - entering...";
				break;
			  }
			}
			LccHolder.LOG.info(str);
			if(lccHolder.getMainChat() != null)
			{
			  lccHolder.getClient().enterChat(lccHolder.getMainChat(), this);
			}
			else
			{
			  LccHolder.LOG.warn("CHAT LISTENER: There is no R1 chat in the list of the public chats");
			}*/
	}

	@Override
	public void onSubscribedChatListReceived(Collection<? extends Chat> chats) {
		String str =
				"CHAT LISTENER: Previously Subscribed Chat List received: user=" + lccHolder.getUser().getUsername() + ", listSize=" + chats.size();
		for (Chat chat : chats) {
			str += "\n\t" + "Chat: roomId=" + chat.getId() + ",\tname=" + chat.getName();
			lccHolder.getClient().enterChat(chat, this);
		}
		LccHolder.LOG.info(str);
	}

	@Override
	public void onChatOpened(Chat chat) {
		LccHolder.LOG.info(
				"CHAT LISTENER: Chat opened: user=" + lccHolder.getUser().getUsername() + ", roomId=" + chat.getId() + ", name=\"" + chat.getName() +
						"\"");
		lccHolder.putGameChat(chat.getGame().getId(), chat);
	}

	@Override
	public void onChatEntered(Chat chat, ChatMember member) {
		LccHolder.LOG.info("CHAT LISTENER: Chat entered: roomId=" + chat.getId() + ", enteredUser=" + member + ", thisUser=" +
				lccHolder.getUser().getUsername());
		/*if(chat.isGameRoom() && member.getUsername().equals(lccHolder.getUser().getUsername()))
			{*/
		lccHolder.putGameChat(chat.getGame().getId(), chat);
		//}
	}

	@Override
	public void onChatExited(Chat chat, ChatMember member) {
	}

	@Override
	public void onChatDisabled(Chat chat, ChatMember member) {
	}

	@Override
	public void onMembersListReceived(Chat chat, Integer membersCount, Collection<ChatMember> members, ChatMember headMember) {
		String str = "CHAT LISTENER: Chat Member List received: roomId=" + chat.getId() + ", user=" + lccHolder.getUser().getUsername() +
				", membersCount=" + membersCount;
		for (ChatMember member : members) {
			str += "\n\tMember: " + member;
		}
		str += (headMember != null ? ("\n\tHead: " + headMember) : StaticData.SYMBOL_EMPTY);
		LccHolder.LOG.info(str);
	}

	@Override
	public void onMessageReceived(Chat chat, ChatMessage message) {
		LccHolder.LOG.info("CHAT LISTENER: Message received: " + message);
		LinkedHashMap<Long, ChatMessage> receivedMessages = lccHolder.getChatMessages(chat.getId());
		if (receivedMessages == null) {
			receivedMessages = new LinkedHashMap<Long, ChatMessage>();
			lccHolder.getReceivedChats().put(chat, receivedMessages);
		}
		if (lccHolder.isUserBlocked(message.getAuthor().getUsername())) {
			LccHolder.LOG.info("CHAT LISTENER: Message received: blocked user");
			return;
		}
		if (chat.isGameRoom() && receivedMessages.put(message.getId(), message) == null) {
			lccHolder.getAndroid().getMainApp().getCurrentGame().values.put(GameItem.HAS_NEW_MESSAGE, "1");
			lccHolder.getAndroid().sendBroadcastIntent(0, IntentConstants.ACTION_GAME_CHAT_MSG);
		}
	}

	@Override
	public void onMessageHistoryReceived(Chat chat, Collection<ChatMessage> messages) {
		String str = "CHAT LISTENER: Chat Message History received: roomId=" + chat.getId() + ", user=" + lccHolder.getUser().getUsername() +
				", messagesCount=" + messages.size();
		for (ChatMessage message : messages) {
			str += "\n\t" + "Message: " + DateTimeUtils.fromDateTime(message.getDateTime()) + ",\tauthor=" +
					message.getAuthor().getUsername() + ",\ttext=\"" + message.getMessage() + "\"";
			onMessageReceived(chat, message);
		}
		LccHolder.LOG.info(str);
	}

	@Override
	public void onMessageRemoved(Chat chat, User by, Long messageId) {
		LccHolder.LOG.info("CHAT LISTENER: Message removed: chat=" + chat + ", by=" + by.getUsername() + ", messageId=" + messageId);

		LinkedHashMap<Long, ChatMessage> receivedMessages = lccHolder.getChatMessages(chat.getId());
		if (receivedMessages != null && receivedMessages.size() != 0) {
			receivedMessages.remove(messageId);
		}
		/*addMessage(chat, by, null);
			for(ChatMessage message : receivedMessages.values())
			{
			  addMessage(chat, message.getAuthor()*//*, message*//*);
    }*/
	}

	@Override
	public void onInvitedToPrivateChat(Chat chat, User by, User invited, Collection<ChatMember> members, ChatMember headMember) {
		LccHolder.LOG.info("CHAT LISTENER: Invited to private chat: chat=" + chat + ", by=" + by.getUsername());

	}

	@Override
	public void onPrivateChatInvitationCancelled(Chat chat, User by) {
		LccHolder.LOG.info("CHAT LISTENER: Private chat invitation cancelled: chat=" + chat + ", by=" + by.getUsername());
	}

	@Override
	public void onPrivateChatInvitationAccepted(Chat chat, User by) {
		LccHolder.LOG.info("CHAT LISTENER: Private chat invitation accepted: chat=" + chat + ", by=" + by.getUsername());
	}

	@Override
	public void onPrivateChatInvitationRejected(Chat chat, User by) {
		LccHolder.LOG.info("CHAT LISTENER: Private chat invitation rejected: chat=" + chat + ", by=" + by.getUsername());
	}

	@Override
	public void onRemovedFromPrivateChat(Chat chat, User by) {
		LccHolder.LOG.info("CHAT LISTENER: Removed from private chat: chat=" + chat + ", by=" + by.getUsername());
	}

	@Override
	public void onVoiceKeyReceived(Chat chat, VoiceRole role, String key) {
		LccHolder.LOG.info("CHAT LISTENER: Voice Key Received: chat=" + chat + ", role=" + role + ", key=" + key);
	}

	@Override
	public void onPublicChatInfoReceived(Map<String, Integer> info) {
		LccHolder.LOG.info("CHAT LISTENER: Public Chat Info Received: info=" + info);
	}

	private final LccHolder lccHolder;
}
