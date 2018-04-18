import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

/**
 * @author pingfangx
 * @date 2018/4/18
 */
public class HelloAction extends AnAction {
    public HelloAction() {
        super("Hello~");
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project=e.getProject();
        Messages.showMessageDialog(project,"Hello~ action","标题",Messages.getInformationIcon());
    }
}
