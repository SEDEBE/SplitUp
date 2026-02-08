package com.splitup.model;

import com.splitup.model.enums.AttachmentType;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "attachments")
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "BIGINT UNSIGNED")
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_id", nullable = false)
    private Expense expense;

    @Enumerated(EnumType.STRING)
    @Column(name = "attachment_type", nullable = false)
    private AttachmentType attachmentType = AttachmentType.RECEIPT_IMAGE;

    @Column(name = "file_path", nullable = false, length = 700)
    private String filePath;

    @Column(name = "mime_type", length = 80)
    private String mimeType;

    @Column(name = "file_size", columnDefinition = "INT UNSIGNED")
    private Integer fileSize;

    @Column(name = "ocr_text", columnDefinition = "TEXT")
    private String ocrText;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public Attachment() {
    }

    public Attachment(Expense expense, String filePath) {
        this.expense = expense;
        this.filePath = filePath;
    }

    public Long getId() {
        return id;
    }

    public Expense getExpense() {
        return expense;
    }

    public AttachmentType getAttachmentType() {
        return attachmentType;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getMimeType() {
        return mimeType;
    }

    public Integer getFileSize() {
        return fileSize;
    }

    public String getOcrText() {
        return ocrText;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setAttachmentType(AttachmentType attachmentType) {
        this.attachmentType = attachmentType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public void setFileSize(Integer fileSize) {
        this.fileSize = fileSize;
    }

    public void setOcrText(String ocrText) {
        this.ocrText = ocrText;
    }
}
