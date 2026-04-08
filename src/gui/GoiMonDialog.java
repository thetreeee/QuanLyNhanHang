package gui;

import entity.Ban;
import entity.NhanVien;

import javax.swing.*;
import java.awt.*;

public class GoiMonDialog extends JDialog {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public GoiMonDialog(Window parent, Ban ban, NhanVien nv) {
        super(parent, "Gọi món - " + ban.getTenBan(), ModalityType.APPLICATION_MODAL);

        setSize(1000, 650);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        add(new GoiMonPanel(ban, nv), BorderLayout.CENTER);
    }
}