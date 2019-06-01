package fi.dy.masa.malilib.gui;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import fi.dy.masa.malilib.gui.Message.MessageType;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import fi.dy.masa.malilib.gui.interfaces.IMessageConsumer;
import fi.dy.masa.malilib.interfaces.ICompletionListener;
import fi.dy.masa.malilib.interfaces.IConfirmationListener;
import fi.dy.masa.malilib.render.RenderUtils;
import fi.dy.masa.malilib.util.StringUtils;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;

public class GuiConfirmAction extends GuiDialogBase implements ICompletionListener
{
    protected final List<String> messageLines = new ArrayList<>();
    protected final IConfirmationListener listener;
    protected int textColor = 0xFFC0C0C0;

    public GuiConfirmAction(int width, String titleKey, IConfirmationListener listener, @Nullable GuiScreen parent, String messageKey, Object... args)
    {
        this.setParent(parent);
        this.title = I18n.format(titleKey);
        this.listener = listener;
        this.useTitleHierarchy = false;
        this.zLevel = 1f;

        StringUtils.splitTextToLines(this.messageLines, I18n.format(messageKey, args), width - 30, this.textRenderer);

        this.setWidthAndHeight(width, this.getMessageHeight() + 50);
        this.centerOnScreen();
    }

    @Override
    public void initGui()
    {
        int x = this.dialogLeft + 10;
        int y = this.dialogTop + this.dialogHeight - 24;
        int buttonWidth = this.getButtonWidth();

        this.createButton(x, y, buttonWidth, ButtonType.OK);
        x += buttonWidth + 10;

        this.createButton(x, y, buttonWidth, ButtonType.CANCEL);

        this.mc.keyboardListener.enableRepeatEvents(true);
    }

    public void setTextColor(int textColor)
    {
        this.textColor = textColor;
    }

    public int getMessageHeight()
    {
        return this.messageLines.size() * (this.textRenderer.FONT_HEIGHT + 1) - 1 + 5;
    }

    protected int getButtonWidth()
    {
        int width = 0;

        for (ButtonType type : ButtonType.values())
        {
            width = Math.max(width, this.getStringWidth(type.getDisplayName()) + 10);
        }

        return width;
    }

    protected void createButton(int x, int y, int buttonWidth, ButtonType type)
    {
        ButtonGeneric button = new ButtonGeneric(x, y, buttonWidth, 20, type.getDisplayName());
        this.addButton(button, this.createActionListener(type));
    }

    @Override
    public boolean doesGuiPauseGame()
    {
        return this.getParent() != null && this.getParent().doesGuiPauseGame();
    }

    @Override
    public void drawContents(int mouseX, int mouseY, float partialTicks)
    {
        if (this.getParent() != null)
        {
            this.getParent().render(mouseX, mouseY, partialTicks);
        }

        GlStateManager.pushMatrix();
        GlStateManager.translatef(0, 0, this.zLevel);

        RenderUtils.drawOutlinedBox(this.dialogLeft, this.dialogTop, this.dialogWidth, this.dialogHeight, 0xF0000000, COLOR_HORIZONTAL_BAR);

        // Draw the title
        this.drawStringWithShadow(this.getTitle(), this.dialogLeft + 10, this.dialogTop + 4, COLOR_WHITE);
        int y = this.dialogTop + 20;

        for (String text : this.messageLines)
        {
            this.drawString(text, this.dialogLeft + 10, y, this.textColor);
            y += this.textRenderer.FONT_HEIGHT + 1;
        }

        this.drawButtons(mouseX, mouseY, partialTicks);
        GlStateManager.popMatrix();
    }

    protected ButtonListener createActionListener(ButtonType type)
    {
        return new ButtonListener(type, this);
    }

    @Override
    public void addMessage(MessageType type, int lifeTime, String messageKey, Object... args)
    {
        if (this.getParent() instanceof IMessageConsumer)
        {
            ((IMessageConsumer) this.getParent()).addMessage(type, lifeTime, messageKey, args);
        }
        else
        {
            super.addMessage(type, lifeTime, messageKey, args);
        }
    }

    @Override
    public void onTaskCompleted()
    {
        if (this.getParent() instanceof ICompletionListener)
        {
            ((ICompletionListener) this.getParent()).onTaskCompleted();
        }
    }

    @Override
    public void onTaskAborted()
    {
        if (this.getParent() instanceof ICompletionListener)
        {
            ((ICompletionListener) this.getParent()).onTaskAborted();
        }
    }

    protected static class ButtonListener implements IButtonActionListener
    {
        private final GuiConfirmAction gui;
        private final ButtonType type;

        public ButtonListener(ButtonType type, GuiConfirmAction gui)
        {
            this.type = type;
            this.gui = gui;
        }

        @Override
        public void actionPerformedWithButton(ButtonBase button, int mouseButton)
        {
            if (this.type == ButtonType.OK)
            {
                this.gui.listener.onActionConfirmed();
            }
            else if (this.type == ButtonType.CANCEL)
            {
                this.gui.listener.onActionCancelled();
            }

            this.gui.mc.displayGuiScreen(this.gui.getParent());
        }
    }

    protected enum ButtonType
    {
        OK      ("malilib.gui.button.ok"),
        CANCEL  ("malilib.gui.button.cancel");

        private final String labelKey;

        private ButtonType(String labelKey)
        {
            this.labelKey = labelKey;
        }

        public String getDisplayName()
        {
            return (this == ButtonType.OK ? GuiBase.TXT_GREEN : GuiBase.TXT_RED) + I18n.format(this.labelKey) + GuiBase.TXT_RST;
        }
    }
}