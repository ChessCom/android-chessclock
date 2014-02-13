package com.chess.lcc.android;

import com.chess.live.client.*;
import com.chess.utilities.LogMe;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class LccChatListener implements ChatListener {

	private static final String TAG = "LCCLOG-CHAT";
	private final LccHelper lccHelper;

	public LccChatListener(LccHelper lccHelper) {
		this.lccHelper = lccHelper;
	}

	@Override
	public void onPublicChatListReceived(Collection<? extends Chat> chats) {
//		LogMe.dl(TAG, "CHAT LISTENER: Public Chat List received: listSize=" + chats.size());
			/*for(Chat chat : chats)
			{
			  str += "\n  " + "Chat: roomId=" + chat.getId() + ", name=\"" + chat.getName() + "\"";
			  if(chat.getId().equals("R1"))
			  {
				lccHelper.setMainChat(chat);
				str += " - entering...";
				break;
			  }
			}
			Log.i(str);
			if(lccHelper.getMainChat() != null)
			{
			  lccHelper.getClient().enterChat(lccHelper.getMainChat(), this);
			}
			else
			{
			  LccHelper.LOG.warn("CHAT LISTENER: There is no R1 chat in the list of the public chats");
			}*/
	}

	// todo: remove
	/*@Override
	public boolean onSubscribedChatListReceived(Collection<? extends Chat> chats) {
		String str =
				"CHAT LISTENER: Previously Subscribed Chat List received: user=" + lccHelper.getUser().getUsername() + ", listSize=" + chats.size();
		for (Chat chat : chats) {
			str += "\n\t" + "Chat: roomId=" + chat.getId() + ",\tname=" + chat.getName();
		}
		LogMe.dl(TAG, str);

		return false;
	}*/

	@Override
	public void onChatOpened(Chat chat) {
//		LogMe.dl(TAG, "CHAT LISTENER: Chat opened: user=" + lccHelper.getUser().getUsername()
//				+ ", roomId=" + chat.getId() + ", name=\"" + chat.getName() + "\"");
		lccHelper.putGameChat(chat.getGame().getId(), chat);
	}

	@Override
	public void onChatEntered(Chat chat, ChatMember member) {
//		LogMe.dl(TAG, "CHAT LISTENER: Chat entered: roomId=" + chat.getId() + ", enteredUser=" + member + ", thisUser=" +
//				lccHelper.getUser().getUsername());
		if (chat.getGame() != null) {
			lccHelper.putGameChat(chat.getGame().getId(), chat);
		}
	}

	@Override
	public void onChatExited(Chat chat, ChatMember member) {
//		LogMe.dl(TAG, "CHAT LISTENER: onChatExited: chat=" + chat + ", member=" + member.getUsername());
	}

	@Override
	public void onChatDisabled(Chat chat, ChatMember member) {
//		LogMe.dl(TAG, "CHAT LISTENER: onChatDisabled: chat=" + chat + ", member=" + member.getUsername());
	}

	@Override
	public void onMembersListReceived(Chat chat, Integer membersCount, Collection<ChatMember> members, ChatMember headMember) {
//		String str = "CHAT LISTENER: Chat Member List received: roomId=" + chat.getId() + ", user=" + lccHelper.getUser().getUsername() +
//				", membersCount=" + membersCount;
//		for (ChatMember member : members) {
//			str += "\n\tMember: " + member;
//		}
//		str += (headMember != null ? ("\n\tHead: " + headMember) : Symbol.EMPTY);
//		LogMe.dl(TAG, str);
	}

	@Override
	public void onMessageReceived(Chat chat, ChatMessage message) {
//		LogMe.dl(TAG, "CHAT LISTENER: Message received: " + message);
		LinkedHashMap<Long, ChatMessage> receivedMessages = lccHelper.getChatMessages(chat.getId());
		if (receivedMessages == null) {
			receivedMessages = new LinkedHashMap<Long, ChatMessage>();
			lccHelper.getReceivedChats().put(chat, receivedMessages);
		}

		if (lccHelper.isUserBlocked(message.getAuthor().getUsername())) {
//			LogMe.dl(TAG, "CHAT LISTENER: Message received: blocked user");
			return;
		}

		if (chat.isGameRoom() && receivedMessages.put(message.getId(), message) == null) {

			if (lccHelper.getLccChatMessageListener() == null) {
//				LogMe.dl(TAG, "CHAT exception check lccHelper.getLccChatMessageListener()");
				return;
			}

			lccHelper.getLccChatMessageListener().onMessageReceived();
		}
	}

	@Override
	public void onMessageHistoryReceived(Chat chat, Collection<ChatMessage> messages) {
//		String str = "CHAT LISTENER: Chat Message History received: roomId=" + chat.getId() + ", user=" + lccHelper.getUser().getUsername() +
//				", messagesCount=" + messages.size();
//		for (ChatMessage message : messages) {
//			str += "\n\t" + "Message: " + DateTimeUtils.fromDateTime(message.getDateTime()) + ",\tauthor=" +
//					message.getAuthor().getUsername() + ",\ttext=\"" + message.getMessage() + "\"";
//			onMessageReceived(chat, message);
//		}
//		LogMe.dl(TAG, str);
	}

	@Override
	public void onMessageRemoved(Chat chat, User by, Long messageId) {
		LogMe.dl(TAG, "CHAT LISTENER: Message removed: chat=" + chat + ", by=" + by.getUsername() + ", messageId=" + messageId);

		LinkedHashMap<Long, ChatMessage> receivedMessages = lccHelper.getChatMessages(chat.getId());
		if (receivedMessages != null && receivedMessages.size() != 0) {
			receivedMessages.remove(messageId);
		}
	}

	@Override
	public void onUserMessagesRemoved(Chat chat, User user, User user1) {
		// todo: UPDATELCC
	}

	@Override
	public void onInvitedToPrivateChat(Chat chat, User by, User invited, Collection<ChatMember> members, ChatMember headMember) {
//		LogMe.dl(TAG, "CHAT LISTENER: Invited to private chat: chat=" + chat + ", by=" + by.getUsername());

	}

	@Override
	public void onPrivateChatInvitationCancelled(Chat chat, User by) {
//		LogMe.dl(TAG, "CHAT LISTENER: Private chat invitation cancelled: chat=" + chat + ", by=" + by.getUsername());
	}

	@Override
	public void onPrivateChatInvitationAccepted(Chat chat, User by) {
//		LogMe.dl(TAG, "CHAT LISTENER: Private chat invitation accepted: chat=" + chat + ", by=" + by.getUsername());
	}

	@Override
	public void onPrivateChatInvitationRejected(Chat chat, User by) {
//		LogMe.dl(TAG, "CHAT LISTENER: Private chat invitation rejected: chat=" + chat + ", by=" + by.getUsername());
	}

	@Override
	public void onRemovedFromPrivateChat(Chat chat, User by) {
//		LogMe.dl(TAG, "CHAT LISTENER: Removed from private chat: chat=" + chat + ", by=" + by.getUsername());
	}

	@Override
	public void onVoiceKeyReceived(Chat chat, VoiceRole role, String key) {
//		LogMe.dl(TAG, "CHAT LISTENER: Voice Key Received: chat=" + chat + ", role=" + role + ", key=" + key);
	}

	@Override
	public void onPublicChatInfoReceived(Map<String, Integer> info) {
//		LogMe.dl(TAG, "CHAT LISTENER: Public Chat Info Received: info=" + info);
	}

}
