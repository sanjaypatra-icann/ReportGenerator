package com.dto;

import java.util.Date;
import java.util.List;

/**
 * Created by srikant.singh on 03/09/2017.
 */
public class Issues {

    private int total;

    private String p;

    private String ps;

    private Paging paging;

    private List<Issue> issues;

    public static class Paging {
        int pageIndex;
        int pageSize;
        int total;

        public int getPageIndex() {
            return pageIndex;
        }

        public void setPageIndex(int pageIndex) {
            this.pageIndex = pageIndex;
        }

        public int getTotal() {
            return total;
        }

        public void setTotal(int total) {
            this.total = total;
        }

        public int getPageSize() {
            return pageSize;
        }

        public void setPageSize(int pageSize) {
            this.pageSize = pageSize;
        }
    }

    public static class Issue {
        String key;
        String rule;
        String severity;
        String component;
        int componentId;
        String project;
        int line;
        TextRange textRange;
        Object[] flows;
        String status;
        String message;
        String effort;
        String debt;
        String author;
        String[] tags;
        Date creationDate;
        Date updateDate;
        String type;

        public static class TextRange {
            int startLine;
            int endLine;
            int startOffset;
            int endOffset;

            public int getStartLine() {
                return startLine;
            }

            public void setStartLine(int startLine) {
                this.startLine = startLine;
            }

            public int getEndLine() {
                return endLine;
            }

            public void setEndLine(int endLine) {
                this.endLine = endLine;
            }

            public int getStartOffset() {
                return startOffset;
            }

            public void setStartOffset(int startOffset) {
                this.startOffset = startOffset;
            }

            public int getEndOffset() {
                return endOffset;
            }

            public void setEndOffset(int endOffset) {
                this.endOffset = endOffset;
            }
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getRule() {
            return rule;
        }

        public void setRule(String rule) {
            this.rule = rule;
        }

        public String getSeverity() {
            return severity;
        }

        public void setSeverity(String severity) {
            this.severity = severity;
        }

        public String getComponent() {
            return component;
        }

        public void setComponent(String component) {
            this.component = component;
        }

        public int getComponentId() {
            return componentId;
        }

        public void setComponentId(int componentId) {
            this.componentId = componentId;
        }

        public String getProject() {
            return project;
        }

        public void setProject(String project) {
            this.project = project;
        }

        public int getLine() {
            return line;
        }

        public void setLine(int line) {
            this.line = line;
        }

        public TextRange getTextRange() {
            return textRange;
        }

        public void setTextRange(TextRange textRange) {
            this.textRange = textRange;
        }

        public Object[] getFlows() {
            return flows;
        }

        public void setFlows(Object[] flows) {
            this.flows = flows;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getEffort() {
            return effort;
        }

        public void setEffort(String effort) {
            this.effort = effort;
        }

        public String getDebt() {
            return debt;
        }

        public void setDebt(String debt) {
            this.debt = debt;
        }

        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        public String[] getTags() {
            return tags;
        }

        public void setTags(String[] tags) {
            this.tags = tags;
        }

        public Date getCreationDate() {
            return creationDate;
        }

        public void setCreationDate(Date creationDate) {
            this.creationDate = creationDate;
        }

        public Date getUpdateDate() {
            return updateDate;
        }

        public void setUpdateDate(Date updateDate) {
            this.updateDate = updateDate;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public String getP() {
        return p;
    }

    public void setP(String p) {
        this.p = p;
    }

    public String getPs() {
        return ps;
    }

    public void setPs(String ps) {
        this.ps = ps;
    }

    public Paging getPaging() {
        return paging;
    }

    public void setPaging(Paging paging) {
        this.paging = paging;
    }

    public List<Issue> getIssue() {
        return issues;
    }

    public void setIssue(List<Issue> issues) {
        this.issues = issues;
    }


    @Override
    public String toString() {
        return "Issues{" +
                "total=" + total +
                ", p='" + p + '\'' +
                ", ps='" + ps + '\'' +
                ", paging=" + paging +
                ", issues=" + issues +
                '}';
    }

}

