        package com.example.demo.domain.chat.controller;

        import com.example.demo.domain.chat.dto.request.*;
        import com.example.demo.domain.chat.dto.response.*;
        import com.example.demo.domain.chat.entity.ChatMessage;
        import com.example.demo.domain.chat.entity.ChatRoom;
        import com.example.demo.domain.chat.exception.ChatRoomNotFoundException;
        import com.example.demo.domain.chat.service.ChatRoomService;
        import com.example.demo.domain.chat.service.ChatService;
        import com.example.demo.domain.post.entity.Post;
        import com.example.demo.domain.post.repository.PostRepository;
        import com.example.demo.domain.report.entity.UserReport;
        import com.example.demo.domain.report.enums.ReportReason;
        import com.example.demo.domain.report.repository.UserReportRepository;
        import com.example.demo.domain.user.entity.User;
        import com.example.demo.domain.user.repository.UserRepository;
        import com.example.demo.global.response.RsData;
        import com.example.demo.global.response.Empty;
        import jakarta.validation.Valid;
        import org.springframework.security.core.Authentication;
        import org.springframework.security.core.annotation.AuthenticationPrincipal;
        import org.springframework.security.core.context.SecurityContextHolder;
        import org.springframework.security.core.userdetails.UserDetails;
        import org.springframework.security.core.userdetails.UsernameNotFoundException;
        import org.springframework.web.bind.annotation.*;
        import org.springframework.web.multipart.MultipartFile;
        import com.example.demo.domain.chat.dto.response.LeaveChatRoomResponseDto;
        import com.example.demo.domain.chat.dto.response.DeleteChatRoomResponseDto;

        import lombok.RequiredArgsConstructor;
        import org.springframework.web.bind.annotation.*;
        import org.springframework.web.multipart.MultipartFile;

        import java.security.Principal;
        import java.time.LocalDateTime;
        import java.util.List;
        import java.util.Optional;
        import java.util.UUID;
        import java.util.stream.Collectors;

        import org.slf4j.Logger;
        import org.slf4j.LoggerFactory;

        @RestController
        @RequiredArgsConstructor
        @CrossOrigin(origins="*")
        @RequestMapping("/api/chat")
        public class ChatController {
            private final ChatRoomService chatRoomService;
            private final ChatService chatService;
            private final UserRepository userRepository;
            private final PostRepository postRepository;
            private final UserReportRepository userReportRepository;
            private static final Logger log = LoggerFactory.getLogger(ChatController.class);


            //채팅방 생성
            @PostMapping("/room")
            public RsData<CreateChatRoomResponseDto> requestChatRoom(@RequestBody CreateChatRoomRequestDto dto) {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication == null || authentication.getName() == null) {
                    log.error("인증 정보가 없습니다. authentication=" + authentication);
                    throw new RuntimeException("인증 정보가 없습니다.");
                }
                String email = authentication.getName();
                log.info("요청한 사용자 email={}", email);

                // 1) 이메일로 유저 조회
                User user = userRepository.findByEmail(email)
                        .orElseThrow(() -> {
                            log.error("User 없는 email={}", email);
                            return new RuntimeException("해당 이메일 사용자를 찾을 수 없습니다. email=" + email);
                        });

                log.info("조회된 userId={}", user.getId());

                // 2) 이제 postId로 채팅방 생성 시도
                try {
                    ChatRoom room = chatRoomService.createOrGetRoom(dto, user.getId());
                    UUID postId = room.getPostId();
                    Optional<Post> postOptional = postRepository.findById(postId);
                    String title = postOptional.map(Post::getTitle).orElse("제목 없음");
                    return new RsData<>("200", "채팅 요청 완료", CreateChatRoomResponseDto.from(room,title));
                } catch(Exception ex) {
                    log.error("채팅방 생성 중 오류. postId={}, buyerId={}", dto.postId(), user.getId(), ex);
                    throw ex; // 필요하면 커스텀 에러로 변환
                }
            }

            // 채팅방 입장(권한 체크)
            @GetMapping("/room/{roomId}")
            public RsData<EnterChatRoomResponseDto.Data> enterChatRoom(
                    @PathVariable UUID roomId,
                    Authentication authentication) {

                if (authentication == null || authentication.getName() == null) {
                    throw new RuntimeException("인증 정보가 없습니다.");
                }

                String email = authentication.getName();

                User user = userRepository.findByEmail(email)
                        .orElseThrow(() -> new RuntimeException("해당 이메일 사용자를 찾을 수 없습니다."));

                UUID userId = user.getId();

                ChatRoom room = chatRoomService.findByRoomId(roomId);

                if (!room.getSender().equals(userId) && !room.getReceiver().equals(userId)) {
                    return new RsData<>("403", "채팅방 입장 권한이 없습니다.", null);
                }

                EnterChatRoomResponseDto.Data data = new EnterChatRoomResponseDto.Data(
                        room.getRoomId().toString(),
                        room.getPostId() != null ? room.getPostId().toString() : null,
                        room.getName(),
                        room.getUserCount(),
                        room.getLastMessage(),
                        room.getLastTimestamp()
                );
                return new RsData<>("200", "채팅방 입장 성공", data);
            }

            // 내 채팅방 목록 조회
            @GetMapping("/rooms")
            public RsData<List<ListChatRoomsResponseDto.ChatRoomDto>> listRooms(Authentication authentication) {
                if (authentication == null || authentication.getName() == null) {
                    throw new RuntimeException("인증 정보가 없습니다.");
                }
                String email = authentication.getName();
                User user = userRepository.findByEmail(email)
                        .orElseThrow(() -> new RuntimeException("해당 이메일 사용자를 찾을 수 없습니다."));
                UUID userId = user.getId();

                List<ChatRoom> rooms = chatRoomService.findRoomByUser(userId);
                List<ListChatRoomsResponseDto.ChatRoomDto> dtos = rooms.stream().map(room -> {
                    // 게시글 제목 조회
                    String postName = null;
                    if (room.getPostId() != null) {
                        postName = postRepository.findById(room.getPostId())
                                .map(Post::getPostName)
                                .orElse("제목 없음");
                    }

                    // 채팅 상대 사용자 닉네임 조회 (자신이 sender면 receiver 정보, receiver면 sender 정보)
                    UUID otherUserId = room.getSender().equals(userId) ? room.getReceiver() : room.getSender();
                    String name = userRepository.findById(otherUserId)
                            .map(User::getName)
                            .orElse("알 수 없음");

                    return new ListChatRoomsResponseDto.ChatRoomDto(
                            room.getRoomId().toString(),
                            room.getPostId() != null ? room.getPostId().toString() : null,
                            name,
                            postName,
                            room.getUserCount(),
                            room.getLastMessage(),
                            room.getLastTimestamp()
                    );
                }).collect(Collectors.toList());

                return new RsData<>("200", "나의 채팅방 목록 조회 성공", dtos);
            }


            // 채팅 메시지 수신
            @GetMapping("/rooms/{roomId}/messages")
            public RsData<ReceiveMessageResponseDto> getMessages(@PathVariable UUID roomId, Authentication authentication) {
                ChatRoom room = chatRoomService.findByRoomId(roomId);

                String userEmail = authentication.getName();
                User user = userRepository.findByEmail(userEmail)
                        .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
                UUID userId = user.getId();

                if (!room.getSender().equals(userId) && !room.getReceiver().equals(userId)) {
                    return new RsData<>("403", "채팅방 입장 권한이 없습니다.", null);
                }

                List<ChatMessage> messages = chatService.getMessages(roomId);

                List<ReceiveMessageResponseDto.Message> messageDtos = messages.stream()
                        .map(m -> new ReceiveMessageResponseDto.Message(
                                m.getId(),
                                m.getChatRoomId(),
                                m.getSender().getId(),
                                m.getContent(),
                                m.getImageUrl(),
                                m.isRead(),
                                m.getSentAt()
                        ))
                        .collect(Collectors.toList());

                ReceiveMessageResponseDto responseDto = new ReceiveMessageResponseDto(userId, messageDtos);

                return new RsData<>("200", "채팅 메시지 목록 조회 성공", responseDto);
            }



            // 채팅 메시지 송신
                @PostMapping("/rooms/messages")
                public RsData<SendMessageResponseDto.Data> sendChatMessage(
                        @RequestBody SendMessageRequestDto request,
                        Authentication authentication) {

                    // 기존 UUID 변환 코드 대신 이메일로 사용자 조회 후 UUID 추출
                    String userEmail = authentication.getName();

                    User senderUser = userRepository.findByEmail(userEmail)
                        .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

                UUID senderId = senderUser.getId();

                // 서비스 호출 시 DTO와 senderId 같이 넘기기
                ChatMessage savedMessage = chatService.sendChatMessage(request, senderId);

                SendMessageResponseDto.Data data = new SendMessageResponseDto.Data(
                        savedMessage.getId(),
                        savedMessage.getChatRoomId(),
                        savedMessage.getSender().getId(),
                        savedMessage.getContent(),
                        savedMessage.getSentAt()
                );

                return new RsData<>("200", "채팅 메시지 전송 완료", data);
            }

            // 이미지 전송
            @PostMapping("/rooms/images")
            public RsData<SendImageResponseDto.Data> sendImageMessage(
                    @ModelAttribute SendImageRequestDto request,
                    Authentication authentication) {

                String userEmail = authentication.getName();
                User senderUser = userRepository.findByEmail(userEmail)
                        .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
                UUID senderId = senderUser.getId();

                ChatMessage imageMessage = chatService.sendImageMessage(request, senderId);

                SendImageResponseDto.Data data = new SendImageResponseDto.Data(
                        imageMessage.getId(),
                        imageMessage.getChatRoomId(),
                        imageMessage.getSender().getId(),
                        imageMessage.getImageUrl(),
                        imageMessage.getSentAt()
                );

                return new RsData<>("200", "이미지 메시지 전송 완료", data);
            }

            //유저 신고
            @PostMapping("/{roomId}/report")
            public RsData<ReportUserResponseDto> reportUser(
                    @PathVariable UUID roomId,
                    @RequestBody @Valid ReportUserRequestDto requestDto,
                    Authentication authentication) {

                String email = authentication.getName();
                User reporter = userRepository.findByEmail(email)
                        .orElseThrow(() -> new RuntimeException("신고자 유저를 찾을 수 없습니다."));

                UserReport report = chatRoomService.reportUserByRoom(roomId, reporter, requestDto.reason());

                ReportUserResponseDto responseDto = new ReportUserResponseDto(
                        report.getId(),
                        roomId,
                        report.getReporter().getId(),
                        report.getReported().getId(),
                        report.getReason(),
                        report.getReportedAt()
                );

                return new RsData<>("200", "신고 완료", responseDto);
            }

            // 채팅방 나가기 (논리 삭제)
            @PostMapping("/rooms/leave")
            public RsData<LeaveChatRoomResponseDto> leaveChatRoom(
                    @RequestBody LeaveChatRoomRequestDto request,
                    Authentication authentication) {
                String email = authentication.getName();
                User user = userRepository.findByEmail(email)
                        .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
                UUID userId = user.getId();

                chatRoomService.leaveChatRoom(request.roomId(), userId);

                int userCount = chatRoomService.getUserCount(request.roomId());

                LeaveChatRoomResponseDto responseDto = new LeaveChatRoomResponseDto(userCount);
                return new RsData<>("200", "채팅방 나가기 완료", responseDto);

            }

            // 채팅방 삭제
            @DeleteMapping("/rooms/remove")
            public RsData<Empty> removeChatRoom(
                    @RequestBody LeaveChatRoomRequestDto request,
                    Authentication authentication) {
                // 인증된 사용자 정보에서 UUID 획득
                String email = authentication.getName();
                User user = userRepository.findByEmail(email)
                        .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
                UUID userId = user.getId();

                // 채팅방 나가기 처리 (soft delete)
                chatRoomService.leaveChatRoom(request.roomId(), userId);

                // 남아있는 사용자 수 조회
                int userCount = chatRoomService.getUserCount(request.roomId());

                // 사용자 0명일 경우 DB와 Redis에서 채팅방 완전 삭제
                if (userCount == 0) {
                    chatRoomService.deleteChatRoomFromDb(request.roomId());
                    chatRoomService.deleteChatRoomFromRedis(request.roomId());
                }

                return new RsData<>("200", "채팅방 완전 삭제 처리 완료", null);
            }

        }

