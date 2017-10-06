package com.tutorial.jira.report;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.plugin.report.impl.AbstractReport;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.web.action.ProjectActionSupport;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.query.Query;
import org.apache.log4j.Logger;
import org.apache.velocity.tools.generic.MathTool;
import webwork.action.ActionContext;

import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.util.*;

/**
 * Created on 3/5/15.
 * Project tutorialplugin.
 * Package com.tutorial.jira.report
 *
 * @author indrajit
 */
public class SampleReport extends AbstractReport {

    private final SearchService searchService;
    private final GroupManager groupManager;

    private final String GROUP_NAME = "reports-access";
    private static final Logger log =
            Logger.getLogger(SampleReport.class);

    public SampleReport(final SearchService searchService,
                        final GroupManager groupManager) {
        this.searchService = searchService;
        this.groupManager = groupManager;
    }

    @Override
    public String generateReportHtml(ProjectActionSupport projectActionSupport, Map params)
            throws Exception {
        return generateReport(projectActionSupport, params, false);
    }

    public String generateReportExcel(ProjectActionSupport projectActionSupport, Map params)
            throws Exception {
        HttpServletResponse response = ActionContext.getResponse();
        response.addHeader("content-disposition",
                "attachment;filename=\"Sample Report.xls\";");
        return generateReport(projectActionSupport, params, true);
    }

    private String generateReport(ProjectActionSupport projectActionSupport, Map params, boolean excelView)
            throws SearchException, ParseException {
        if (!isAccessAllowedForUser(projectActionSupport.getLoggedInUser())) {
            TreeMap<String, Object> restrictedViewParams = new TreeMap<String, Object>();
            restrictedViewParams.put("GROUP_NAME", GROUP_NAME);
            restrictedViewParams.put("USER_NAME", projectActionSupport.getLoggedInUser().getDisplayName());
            restrictedViewParams.put("USER_ID", projectActionSupport.getLoggedInUser().getName());
            restrictedViewParams.put("BASE_URL",
                    ComponentAccessor.getApplicationProperties().getString(APKeys.JIRA_BASEURL));
            return descriptor.getHtml("restricted", restrictedViewParams);
        }
        String projectId = (String) params.get("selectedProjectId");
        try {
            List<Issue> issues = getIssues(projectActionSupport.getLoggedInUser(), projectId);

            Collection<HashMap<String, Object>> issueDetails = new ArrayList<HashMap<String, Object>>();
            for (Issue issue : issues) {
                HashMap<String, Object> issueMap = new HashMap<String, Object>();

                issueMap.put("key", issue.getKey());
                issueMap.put("summary", issue.getSummary());
                issueMap.put("reporter", issue.getReporter());
                issueMap.put("assignee", issue.getAssignee());
                issueMap.put("originalEstimate", issue.getOriginalEstimate() / 3600.0);
                issueMap.put("timeSpent", issue.getTimeSpent() / 3600.0);
                issueMap.put("remainingEstimate", issue.getEstimate() / 3600.0);
                issueDetails.add(issueMap);
            }

            TreeMap<String, Object> velocityParams = new TreeMap<String, Object>();
            velocityParams.put("BASE_URL",
                    ComponentAccessor.getApplicationProperties().getString(APKeys.JIRA_BASEURL));
            velocityParams.put("issueDetails", issueDetails);
            velocityParams.put("math", new MathTool());
            return excelView ? descriptor.getHtml("excel", velocityParams) : descriptor.getHtml("view", velocityParams);
        } catch (Exception ex) {
            return ex.getMessage();
        }
    }

    private List<Issue> getIssues(User user, String projectId)
            throws SearchException, ParseException {
        JqlQueryBuilder queryBuilder = JqlQueryBuilder.newBuilder();
        Query query = queryBuilder.where().project(projectId).buildQuery();
        if (searchService != null) {
            SearchResults result = searchService.search(user, query, PagerFilter.getUnlimitedFilter());
            if (result != null) {
                return result.getIssues();
            }
        }
        return null;
    }

    @Override
    public boolean isExcelViewSupported() {
        return true;
    }

    public boolean isAccessAllowedForUser(User user) {
        Collection<String> groupNames = groupManager.getGroupNamesForUser(user);
        return groupNames.contains(GROUP_NAME);
    }

}
