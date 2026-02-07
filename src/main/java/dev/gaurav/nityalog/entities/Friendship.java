package dev.gaurav.nityalog.entities;

import dev.gaurav.nityalog.enums.FriendshipStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
        name = "friendships",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_friendships__user_low_user_high",
                        columnNames = {"user_low_id", "user_high_id"}
                )
        },
        indexes = {
                @Index(name = "idx_friendship_user_low", columnList = "user_low_id"),
                @Index(name = "idx_friendship_user_high", columnList = "user_high_id"),
                @Index(name = "idx_friendship_status", columnList = "status"),
                @Index(name = "idx_friendship_requested_by", columnList = "requested_by")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Friendship extends BaseEntity {

    /** Smaller UUID user */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_low_id", nullable = false)
    private User userLow;

    /** Bigger UUID user */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_high_id", nullable = false)
    private User userHigh;

    /** Who initiated the request */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requested_by", nullable = false)
    private User requestedBy;

    /** Current friendship state */
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FriendshipStatus status = FriendshipStatus.PENDING;

    /** When the request was accepted */
    @Column(name = "accepted_at")
    private Instant acceptedAt;

    /** Optional message sent with request */
    @Column(length = 255)
    private String message;

    /** Who performed the last action (accept / reject / block) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "action_by")
    private User actionBy;

    public void accept(User actor) {
        if (this.status != FriendshipStatus.PENDING) {
            throw new IllegalStateException("Only pending request can be accepted");
        }
        this.status = FriendshipStatus.ACCEPTED;
        this.acceptedAt = Instant.now();
        this.actionBy = actor;
    }

    public void reject(User actor) {
        this.status = FriendshipStatus.REJECTED;
        this.actionBy = actor;
    }

    public void block(User actor) {
        this.status = FriendshipStatus.BLOCKED;
        this.actionBy = actor;
    }

    public boolean isAccepted() {
        return this.status == FriendshipStatus.ACCEPTED;
    }
}
