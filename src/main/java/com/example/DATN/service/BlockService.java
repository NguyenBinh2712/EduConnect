package com.example.DATN.service;

import com.example.DATN.entity.Block;
import com.example.DATN.entity.User;
import com.example.DATN.exception.AppException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.repository.BlockRepository;
import com.example.DATN.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class BlockService {
    BlockRepository blockRepository;
    UserRepository userRepository;

    public boolean isBlocked(Long blocker,Long blocked){
         return blockRepository.existsByBlockerIdAndBlockedId(blocker,blocked) ||
                blockRepository.existsByBlockerIdAndBlockedId(blocked,blocker);
    }

    public void blockUser(Long blockerId,Long blockedId){
        if (blockerId.equals(blockedId)){
            throw new AppException(ErrorCode.NO_PERMISSION);
        }
        if(isBlocked(blockerId,blockedId)){
            return;
        }
        User blocker=userRepository.findById(blockerId).orElseThrow(()->new AppException(ErrorCode.USER_NOT_EXISTED));
        User blocked=userRepository.findById(blockedId).orElseThrow(()->new AppException(ErrorCode.USER_NOT_EXISTED));

        Block block=Block.builder()
                .blocker(blocker)
                .blocked(blocked)
                .build();
        blockRepository.save(block);
    }
    public void unblockUser(Long blockerId, Long blockedId) {
        Block block=blockRepository.findByBlockerIdAndBlockedId(blockerId,blockedId)
                .orElseThrow(()->new AppException(ErrorCode.BLOCK_NOT_FOUND));
        blockRepository.delete(block);
    }
    public List<Long> getMyBlocked(Long blockerId){
        return blockRepository.findBlockedIdsByBlockerId(blockerId);
    }

    public Set<Long> getAllHiddenUserIds(Long userId){
        Set<Long> result = new HashSet<>();

        result.addAll(blockRepository.findBlockedIdsByBlockerId(userId));
        result.addAll(blockRepository.findBlockerIdsByBlockedId(userId));

        return result;
    }
}
