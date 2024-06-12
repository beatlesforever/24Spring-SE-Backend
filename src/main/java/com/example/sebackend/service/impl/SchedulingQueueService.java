package com.example.sebackend.service.impl;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
@Slf4j
public class SchedulingQueueService {
    private final ConcurrentLinkedQueue<Integer> schedulingQueue;

    @Autowired
    public SchedulingQueueService(ConcurrentLinkedQueue<Integer> schedulingQueue) {
        this.schedulingQueue = schedulingQueue;
    }

    /**
     * 将房间ID添加到调度队列中。
     * 将请求加入到请求队列中,并删除之前的同一房间的等待中的请求
     * @param roomId 房间ID，将被添加到调度队列的尾部。
     */
    public synchronized void addRoomToQueue(int roomId) {
        // 删除之前的同一房间的等待中的请求
        Iterator<Integer> iterator = schedulingQueue.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().equals(roomId)) {
                iterator.remove();
            }
        }
        // 添加新的请求到队列中
        schedulingQueue.offer(roomId);
    }

    /**
     * 从调度队列中移除并返回第一个房间ID。
     *
     * @return 调度队列中的第一个房间ID。如果队列为空，则返回null。
     */
    public Integer removeFirstRoomFromQueue() {
        log.info("remove first room from queue");
        return schedulingQueue.poll();
    }

    /**
     * 检查调度队列是否为空。
     *
     * @return 如果调度队列为空，则返回true；否则返回false。
     */
    public boolean isQueueEmpty() {
        return schedulingQueue.isEmpty();
    }

    public void removeRoomFromQueue(Integer roomId) {
        log.info("remove room from queue: " + roomId);
        Iterator<Integer> iterator = schedulingQueue.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().equals(roomId)) {
                iterator.remove();
            }
        }
    }

    public String getQueueSize() {
        return String.valueOf(schedulingQueue.size());
    }
}
