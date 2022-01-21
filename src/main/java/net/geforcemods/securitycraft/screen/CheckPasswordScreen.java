package net.geforcemods.securitycraft.screen;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.geforcemods.securitycraft.SecurityCraft;
import net.geforcemods.securitycraft.inventory.GenericTEMenu;
import net.geforcemods.securitycraft.network.server.CheckPassword;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.client.gui.widget.ExtendedButton;

public class CheckPasswordScreen extends AbstractContainerScreen<GenericTEMenu> {
	private static final ResourceLocation TEXTURE = new ResourceLocation("securitycraft:textures/gui/container/blank.png");
	private BlockEntity be;
	private char[] allowedChars = {
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '\u0008', '\u001B'
	}; //0-9, backspace and escape
	private TranslatableComponent blockName;
	private EditBox keycodeTextbox;
	private String currentString = "";
	private static final int MAX_CHARS = 20;

	public CheckPasswordScreen(GenericTEMenu menu, Inventory inv, Component title) {
		super(menu, inv, title);
		this.be = menu.be;
		blockName = Utils.localize(be.getBlockState().getBlock().getDescriptionId());
	}

	@Override
	public void init() {
		super.init();
		minecraft.keyboardHandler.setSendRepeatsToGui(true);

		addRenderableWidget(new ExtendedButton(width / 2 - 38, height / 2 + 30 + 10, 80, 20, new TextComponent("0"), b -> addNumberToString(0)));
		addRenderableWidget(new ExtendedButton(width / 2 - 38, height / 2 - 60 + 10, 20, 20, new TextComponent("1"), b -> addNumberToString(1)));
		addRenderableWidget(new ExtendedButton(width / 2 - 8, height / 2 - 60 + 10, 20, 20, new TextComponent("2"), b -> addNumberToString(2)));
		addRenderableWidget(new ExtendedButton(width / 2 + 22, height / 2 - 60 + 10, 20, 20, new TextComponent("3"), b -> addNumberToString(3)));
		addRenderableWidget(new ExtendedButton(width / 2 - 38, height / 2 - 30 + 10, 20, 20, new TextComponent("4"), b -> addNumberToString(4)));
		addRenderableWidget(new ExtendedButton(width / 2 - 8, height / 2 - 30 + 10, 20, 20, new TextComponent("5"), b -> addNumberToString(5)));
		addRenderableWidget(new ExtendedButton(width / 2 + 22, height / 2 - 30 + 10, 20, 20, new TextComponent("6"), b -> addNumberToString(6)));
		addRenderableWidget(new ExtendedButton(width / 2 - 38, height / 2 + 10, 20, 20, new TextComponent("7"), b -> addNumberToString(7)));
		addRenderableWidget(new ExtendedButton(width / 2 - 8, height / 2 + 10, 20, 20, new TextComponent("8"), b -> addNumberToString(8)));
		addRenderableWidget(new ExtendedButton(width / 2 + 22, height / 2 + 10, 20, 20, new TextComponent("9"), b -> addNumberToString(9)));
		addRenderableWidget(new ExtendedButton(width / 2 + 48, height / 2 + 30 + 10, 25, 20, new TextComponent("<-"), b -> removeLastCharacter()));

		addRenderableWidget(keycodeTextbox = new EditBox(font, width / 2 - 37, height / 2 - 67, 77, 12, TextComponent.EMPTY));
		keycodeTextbox.setMaxLength(MAX_CHARS);
		keycodeTextbox.setFilter(s -> s.matches("[0-9]*\\**")); //allow any amount of numbers and any amount of asterisks
		setInitialFocus(keycodeTextbox);
	}

	@Override
	public void removed() {
		super.removed();
		minecraft.keyboardHandler.setSendRepeatsToGui(false);
	}

	@Override
	protected void renderLabels(PoseStack pose, int mouseX, int mouseY) {
		font.draw(pose, blockName, imageWidth / 2 - font.width(blockName) / 2, 6, 4210752);
	}

	@Override
	protected void renderBg(PoseStack pose, float partialTicks, int mouseX, int mouseY) {
		int startX = (width - imageWidth) / 2;
		int startY = (height - imageHeight) / 2;

		renderBackground(pose);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem._setShaderTexture(0, TEXTURE);
		blit(pose, startX, startY, 0, 0, imageWidth, imageHeight);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == GLFW.GLFW_KEY_BACKSPACE && currentString.length() > 0) {
			Minecraft.getInstance().player.playSound(SoundEvents.UI_BUTTON_CLICK, 0.15F, 1.0F);
			currentString = Utils.removeLastChar(currentString);
			setTextboxCensoredText(keycodeTextbox, currentString);
			checkCode(currentString);
			return true;
		}

		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public boolean charTyped(char typedChar, int keyCode) {
		if (isValidChar(typedChar) && currentString.length() < MAX_CHARS) {
			Minecraft.getInstance().player.playSound(SoundEvents.UI_BUTTON_CLICK, 0.15F, 1.0F);
			currentString += typedChar;
			setTextboxCensoredText(keycodeTextbox, currentString);
			checkCode(currentString);
		}
		else
			return super.charTyped(typedChar, keyCode);

		return true;
	}

	private boolean isValidChar(char c) {
		for (int i = 0; i < allowedChars.length; i++) {
			if (c == allowedChars[i])
				return true;
		}

		return false;
	}

	private void addNumberToString(int number) {
		if (currentString.length() < MAX_CHARS) {
			currentString += "" + number;
			setTextboxCensoredText(keycodeTextbox, currentString);
			checkCode(currentString);
		}
	}

	private void removeLastCharacter() {
		if (currentString.length() > 0) {
			currentString = Utils.removeLastChar(currentString);
			setTextboxCensoredText(keycodeTextbox, currentString);
		}
	}

	private void setTextboxCensoredText(EditBox textField, String text) {
		String x = "";

		for (int i = 1; i <= text.length(); i++) {
			x += "*";
		}

		textField.setValue(x);
	}

	public void checkCode(String code) {
		SecurityCraft.channel.sendToServer(new CheckPassword(be.getBlockPos().getX(), be.getBlockPos().getY(), be.getBlockPos().getZ(), code));
	}
}
