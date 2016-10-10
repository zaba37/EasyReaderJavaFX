package com.zaba37.easyreader.models;

import com.zaba37.easyreader.textEditor.ParStyle;
import com.zaba37.easyreader.textEditor.TextStyle;
import org.fxmisc.richtext.StyledTextArea;
import org.fxmisc.richtext.model.EditableStyledDocument;

import java.util.function.BiConsumer;

/**
 * Created by zaba3 on 05.10.2016.
 */
public class page extends StyledTextArea {
    public page(Object initialParagraphStyle, BiConsumer applyParagraphStyle, Object initialTextStyle, BiConsumer applyStyle) {
        super(initialParagraphStyle, applyParagraphStyle, initialTextStyle, applyStyle);
    }

    public page(Object initialParagraphStyle, BiConsumer applyParagraphStyle, Object initialTextStyle, BiConsumer applyStyle, boolean preserveStyle) {
        super(initialParagraphStyle, applyParagraphStyle, initialTextStyle, applyStyle, preserveStyle);
    }

    public page(Object initialParagraphStyle, BiConsumer applyParagraphStyle, Object initialTextStyle, BiConsumer applyStyle, EditableStyledDocument document) {
        super(initialParagraphStyle, applyParagraphStyle, initialTextStyle, applyStyle, document);
    }

    public page(Object initialParagraphStyle, BiConsumer applyParagraphStyle, Object initialTextStyle, BiConsumer applyStyle, EditableStyledDocument document, boolean preserveStyle) {
        super(initialParagraphStyle, applyParagraphStyle, initialTextStyle, applyStyle, document, preserveStyle);
    }
}
