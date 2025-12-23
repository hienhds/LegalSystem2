package com.example.chatservice.service;

import com.example.chatservice.dto.request.SendFileMessageRequest;
import com.example.chatservice.dto.request.SendTextMessageRequest;
import com.example.chatservice.dto.response.*;
import com.example.chatservice.event.MessageEvent;
import com.example.chatservice.exception.AppException;
import com.example.chatservice.exception.ErrorType;
import com.example.chatservice.grpc.FileServiceGrpcClient;
import com.example.chatservice.grpc.PresignedUrlResponse;
import com.example.chatservice.jpa.entity.Conversation;
import com.example.chatservice.jpa.entity.ConversationMember;
import com.example.chatservice.jpa.repository.ConversationMemberRepository;
import com.example.chatservice.jpa.repository.ConversationRepository;
import com.example.chatservice.kafka.ChatEventProducer;
import com.example.chatservice.mongo.document.Message;
import com.example.chatservice.mongo.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository messageRepository;
    private final ConversationMemberRepository memberRepository;
    private final ConversationRepository conversationRepository;
    private final ChatEventProducer chatEventProducer;
    private final FileServiceGrpcClient fileServiceGrpcClient;
    @Transactional
    public MessageResponse sendTextMessage(
            String conversationId,
            Long senderId,
            String fullName,
            String avatar,
            SendTextMessageRequest request
    ){
        // validate phongf con hoat dong isActive khong

        boolean  isSender = memberRepository.existsByConversation_IdAndUserIdAndMemberStatusIn(
                conversationId,
                senderId,
                List.of(
                        ConversationMember.MemberStatus.OWNER,
                        ConversationMember.MemberStatus.MEMBER
                )
        );

        if(!isSender){
            throw new AppException(ErrorType.FORBIDDEN, "banj khong la thanh vien");
        }

        if(request.getContent() == null || request.getContent().isBlank()){
            throw new AppException(ErrorType.BAD_REQUEST, "Message rỗng");
        }

        Message message = new Message();

        message.setConversationId(conversationId);
        message.setSenderId(senderId);
        message.setSenderName(fullName);
        message.setSenderAvatar(avatar);
        message.setContent(request.getContent());
        message.setType(Message.MessageType.TEXT);
        message.setTimestamp(LocalDateTime.now());
        message.setStatus(Message.MessageStatus.SENT);

        messageRepository.save(message);

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(()-> new AppException(ErrorType.NOT_FOUND, "khong tim thay phongf"));

        conversation.setLastMessageAt(message.getTimestamp());
        conversation.setLastMessageText(message.getContent());
        conversation.setLastMessageSenderName(message.getSenderName());

        conversationRepository.save(conversation);

        MessageEvent event = MessageEvent.builder()
                .id(message.getId())
                .conversationId(message.getId())
                .senderId(message.getSenderId())
                .senderName(message.getSenderName())
                .senderAvatar(message.getSenderAvatar())
                .type(message.getType())
                .content(message.getContent())
                .timestamp(message.getTimestamp())
                .build();

        chatEventProducer.publishMessageCreated(event);

        return MessageResponse.builder()
                .messageId(message.getId())
                .conversationId(conversationId)
                .senderId(senderId)
                .senderName(fullName)
                .senderAvatar(avatar)
                .type(message.getType())
                .content(message.getContent())
                .timestamp(message.getTimestamp())
                .status(message.getStatus())
                .build();


    }

    // send file
    @Transactional
    public PresignedFileResponse generateFileUploadUrl(
            String conversationId,
            Long senderId,
            String fileName,
            String contentType,
            long fileSize
    ){
        // validate phongf con hoat dong isActive khong

        boolean  isSender = memberRepository.existsByConversation_IdAndUserIdAndMemberStatusIn(
                conversationId,
                senderId,
                List.of(
                        ConversationMember.MemberStatus.OWNER,
                        ConversationMember.MemberStatus.MEMBER
                )
        );

        if(!isSender){
            throw new AppException(ErrorType.FORBIDDEN, "banj khong la thanh vien");
        }

        PresignedUrlResponse grpcResponse = fileServiceGrpcClient.generateUploadUrl(
                fileName,
                contentType,
                fileSize,
                "GROUP_SEND_FILE",
                conversationId,
                senderId
        );

        if (!grpcResponse.getSuccess()) {
            throw new AppException(ErrorType.INTERNAL_ERROR, grpcResponse.getErrorMessage());
        }

        return PresignedFileResponse.builder()
                .fileId(grpcResponse.getFileId())
                .uploadUrl(grpcResponse.getPresignedUrl())
                .expiredAt(grpcResponse.getExpiredAt())
                .build();

    }

    @Transactional
    public PresignedFileResponse generateFilePreviewUrl(
            String conversationId,
            Long senderId,
            String fileId
    ){
        // validate phongf con hoat dong isActive khong

        boolean  isSender = memberRepository.existsByConversation_IdAndUserIdAndMemberStatusIn(
                conversationId,
                senderId,
                List.of(
                        ConversationMember.MemberStatus.OWNER,
                        ConversationMember.MemberStatus.MEMBER
                )
        );

        if(!isSender){
            throw new AppException(ErrorType.FORBIDDEN, "banj khong la thanh vien");
        }

        PresignedUrlResponse grpcResponse = fileServiceGrpcClient.getPreviewUrl(fileId);

        if (!grpcResponse.getSuccess()) {
            throw new AppException(ErrorType.INTERNAL_ERROR, grpcResponse.getErrorMessage());
        }

        return PresignedFileResponse.builder()
                .fileId(grpcResponse.getFileId())
                .uploadUrl(grpcResponse.getPresignedUrl())
                .expiredAt(grpcResponse.getExpiredAt())
                .build();

    }

    @Transactional
    public PresignedFileResponse generateFileDownloadUrl(
            String conversationId,
            Long senderId,
            String fileId,
            String fileName
    ){
        // validate phongf con hoat dong isActive khong

        boolean  isSender = memberRepository.existsByConversation_IdAndUserIdAndMemberStatusIn(
                conversationId,
                senderId,
                List.of(
                        ConversationMember.MemberStatus.OWNER,
                        ConversationMember.MemberStatus.MEMBER
                )
        );

        if(!isSender){
            throw new AppException(ErrorType.FORBIDDEN, "banj khong la thanh vien");
        }

        PresignedUrlResponse grpcResponse = fileServiceGrpcClient.getDownloadUrl(fileId, fileName);

        if (!grpcResponse.getSuccess()) {
            throw new AppException(ErrorType.INTERNAL_ERROR, grpcResponse.getErrorMessage());
        }

        return PresignedFileResponse.builder()
                .fileId(grpcResponse.getFileId())
                .uploadUrl(grpcResponse.getPresignedUrl())
                .expiredAt(grpcResponse.getExpiredAt())
                .build();

    }




    private static record CursorData(
            LocalDateTime time,
            String id
    ) {}

    private static final DateTimeFormatter CURSOR_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

    private CursorData parseCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }

        try {
            String decoded = new String(
                    Base64.getUrlDecoder().decode(cursor),
                    StandardCharsets.UTF_8
            );

            String[] parts = decoded.split("\\|");
            if (parts.length != 2) {
                throw new IllegalArgumentException();
            }

            LocalDateTime time = LocalDateTime.parse(
                    parts[0],
                    CURSOR_TIME_FORMATTER
            );

            return new CursorData(time, parts[1]);
        } catch (Exception e) {
            throw new AppException(
                    ErrorType.BAD_REQUEST,
                    "Cursor không hợp lệ"
            );
        }
    }

    private String encodeCursor(Message message) {
        String raw = CURSOR_TIME_FORMATTER.format(message.getTimestamp())
                + "|" + message.getId();

        return Base64.getUrlEncoder()
                .encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    /* ================= SERVICE ================= */

    @Transactional(readOnly = true)
    public MessageListResponse getMessages(
            String conversationId,
            int limit,
            String cursor
    ) {
        if (limit <= 0 || limit > 100) {
            throw new AppException(
                    ErrorType.BAD_REQUEST,
                    "Limit không hợp lệ"
            );
        }

        Pageable pageable = PageRequest.of(0, limit + 1);
        CursorData cursorData = parseCursor(cursor);

        List<Message> messages;

        if (cursorData == null) {
            // Load newest messages
            messages = messageRepository
                    .findByConversationIdOrderByTimestampDesc(
                            conversationId,
                            pageable
                    );
        } else {
            // Load older messages using cursor
            messages = messageRepository
                    .findOlderMessages(
                            conversationId,
                            cursorData.time(),
                            cursorData.id(),
                            pageable
                    );
        }

        boolean hasMore = messages.size() > limit;
        if (hasMore) {
            messages = messages.subList(0, limit);
        }

        List<MessageResponse> items = messages.stream()
                .map(this::toResponse)
                .toList();

        String nextCursor = hasMore
                ? encodeCursor(messages.get(messages.size() - 1))
                : null;

        return MessageListResponse.builder()
                .items(items)
                .hasMore(hasMore)
                .nextCursor(nextCursor)
                .build();
    }

    /* ================= MAPPER ================= */

    private MessageResponse toResponse(Message message) {
        return MessageResponse.builder()
                .messageId(message.getId())
                .conversationId(message.getConversationId())
                .senderId(message.getSenderId())
                .senderName(message.getSenderName())
                .senderAvatar(message.getSenderAvatar())
                .type(message.getType())
                .content(message.getContent())
                .file(message.getFile())
                .timestamp(message.getTimestamp())
                .status(message.getStatus())
                .build();
    }

    @Transactional(readOnly = true)
    public ConversationFileListResponse getConversationFiles(
            String conversationId,
            Long userId,
            int limit,
            String cursor
    ) {
        boolean isMember = memberRepository
                .existsByConversation_IdAndUserIdAndMemberStatusIn(
                        conversationId,
                        userId,
                        List.of(
                                ConversationMember.MemberStatus.OWNER,
                                ConversationMember.MemberStatus.MEMBER
                        )
                );

        if (!isMember) {
            throw new AppException(ErrorType.FORBIDDEN, "Bạn không phải thành viên");
        }

        if (limit <= 0 || limit > 50) {
            throw new AppException(ErrorType.BAD_REQUEST, "Limit không hợp lệ");
        }

        Pageable pageable = PageRequest.of(0, limit + 1);
        CursorData cursorData = parseCursor(cursor);

        List<Message> messages;

        if (cursorData == null) {
            messages = messageRepository
                    .findByConversationIdAndTypeOrderByTimestampDesc(
                            conversationId,
                            Message.MessageType.FILE,
                            pageable
                    );
        } else {
            messages = messageRepository
                    .findOlderFileMessages(
                            conversationId,
                            Message.MessageType.FILE,
                            cursorData.time(),
                            cursorData.id(),
                            pageable
                    );
        }

        boolean hasMore = messages.size() > limit;
        if (hasMore) {
            messages = messages.subList(0, limit);
        }

        List<ConversationFileResponse> items = messages.stream()
                .map(msg -> ConversationFileResponse.builder()
                        .messageId(msg.getId())
                        .fileId(msg.getFile().getFileId())
                        .fileName(msg.getFile().getFileName())
                        .fileSize(msg.getFile().getFileSize())
                        .contentType(msg.getFile().getContentType())
                        .senderId(msg.getSenderId())
                        .senderName(msg.getSenderName())
                        .sentAt(msg.getTimestamp())
                        .build()
                )
                .toList();

        String nextCursor = hasMore
                ? encodeCursor(messages.get(messages.size() - 1))
                : null;

        return ConversationFileListResponse.builder()
                .items(items)
                .hasMore(hasMore)
                .nextCursor(nextCursor)
                .build();
    }


}
