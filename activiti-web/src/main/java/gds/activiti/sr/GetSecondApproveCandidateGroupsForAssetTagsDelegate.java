package gds.activiti.sr;

import static gds.activiti.sr.constants.RoleConstants.PROJECT_CREATE_REVIEWER;
import static gds.activiti.sr.constants.RoleConstants.TECH_TASK_CONTENT_REVIEWER;
import static gds.activiti.sr.constants.TagConstants.TAG_PROJRCT_CREATE_CONTENT;
import static gds.activiti.sr.constants.TagConstants.TAG_TECH_TASK_CONTENT;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import net.emforge.activiti.content.LiferayAssetsUtil;
import net.emforge.activiti.identity.LiferayGroupsUtil;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.model.Team;
import com.liferay.portal.service.TeamLocalServiceUtil;

/**
 * 该类通常用在流程定义文件中，用来根据内容类型来选择候选用户组
 * 
 * @author wangf
 */
public class GetSecondApproveCandidateGroupsForAssetTagsDelegate implements JavaDelegate {
	
	private static Log _log = LogFactoryUtil.getLog(GetSecondApproveCandidateGroupsForAssetTagsDelegate.class);

	private LiferayGroupsUtil liferayGroupsUtil;
	private LiferayAssetsUtil liferayAssetsUtil;
	
	public GetSecondApproveCandidateGroupsForAssetTagsDelegate() {
		liferayGroupsUtil = new LiferayGroupsUtil();
		liferayAssetsUtil = new LiferayAssetsUtil();
	}
	
	@Override
	public void execute(DelegateExecution execution) throws Exception {
		
		// 获取启动流程时提交内容的标签
		Collection<String> assetTags = liferayAssetsUtil.getAssetTags(execution);
		
		// 获取与标签相对应的审核候选组
		Collection<String> cadidateGroupsForTags = getCadidateGroupsForTags(execution, assetTags);

		// 设置流程变量，流程中的UserTask中使用该流程变量来安排审核人
		execution.setVariable("secondCandidateGroupList", cadidateGroupsForTags);
	}
	
	private Collection<String> getCadidateGroupsForTags(DelegateExecution execution, Collection<String> assetTags) throws Exception {
		
		List<String> candidateRoleList = liferayGroupsUtil.getDefaultApproverGroups();
		long companyId = GetterUtil.getLong((Serializable)execution.getVariable(WorkflowConstants.CONTEXT_COMPANY_ID));
		long groupId = GetterUtil.getLong((Serializable)execution.getVariable(WorkflowConstants.CONTEXT_GROUP_ID));
		List<Team> allTeams=TeamLocalServiceUtil.getGroupTeams(groupId);
		for (Team team : allTeams) {
			_log.info(team.getName());
		}
		
		//如果是技术保障任务
		if (assetTags.contains(TAG_TECH_TASK_CONTENT)) {
			candidateRoleList.add(TECH_TASK_CONTENT_REVIEWER);
			_log.info("添加了角色: " + TECH_TASK_CONTENT_REVIEWER);
		}
		//如果是项目立项
		if (assetTags.contains(TAG_PROJRCT_CREATE_CONTENT)) {
			candidateRoleList.add(PROJECT_CREATE_REVIEWER);
			_log.info("添加了角色: " + PROJECT_CREATE_REVIEWER);
		}
		
		return candidateRoleList;
	}

}
