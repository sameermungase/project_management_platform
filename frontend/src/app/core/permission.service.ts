import { Injectable } from '@angular/core';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class PermissionService {
  private roleHierarchy: { [key: string]: number } = {
    'SDE_1': 1,
    'SDE_2': 2,
    'SENIOR': 3,
    'STAFF': 4,
    'PRINCIPAL': 5,
    'ARCHITECT': 6,
    'ADMIN': 10,
    'TECHNICAL_LEAD': 3, // Map to SENIOR level
    'MANAGER': 2, // Map to SDE_2 level
    'USER': 1 // Map to SDE_1 level
  };

  constructor(private authService: AuthService) {}

  /**
   * Get user's role from current user or project context
   */
  getUserRole(projectRole?: string): string {
    if (projectRole) {
      return projectRole;
    }
    const user = this.authService.currentUserValue;
    if (user && user.roles && user.roles.length > 0) {
      // Return highest role
      return user.roles.reduce((highest: string, role: string) => {
        const currentLevel = this.roleHierarchy[role] || 0;
        const highestLevel = this.roleHierarchy[highest] || 0;
        return currentLevel > highestLevel ? role : highest;
      }, user.roles[0]);
    }
    return 'USER';
  }

  /**
   * Check if user can perform action
   */
  canPerformAction(action: string, projectRole?: string): boolean {
    const userRole = this.getUserRole(projectRole);
    const requiredRole = this.getRequiredRole(action);
    const userLevel = this.roleHierarchy[userRole] || 0;
    const requiredLevel = this.roleHierarchy[requiredRole] || 0;
    return userLevel >= requiredLevel;
  }

  /**
   * Get required role for an action
   */
  private getRequiredRole(action: string): string {
    const actionMap: { [key: string]: string } = {
      'CREATE_EPIC': 'SENIOR',
      'CREATE_MILESTONE': 'SENIOR',
      'CREATE_TASK': 'SDE_2',
      'ASSIGN_TASK': 'SDE_2',
      'APPROVE_PR': 'SENIOR',
      'APPROVE_DESIGN': 'STAFF',
      'APPROVE_ARCHITECTURE': 'ARCHITECT',
      'ESTIMATE_TIMELINE': 'SDE_2',
      'OVERRIDE_DECISION': 'STAFF',
      'UPDATE_TASK_STATUS': 'SDE_1',
      'REQUEST_APPROVAL': 'SDE_1',
      'APPROVE_APPROVALS': 'SENIOR',
      'VIEW_APPROVALS': 'SDE_1',
      'VIEW_DECISIONS': 'SDE_1'
    };
    return actionMap[action] || 'ARCHITECT';
  }

  /**
   * Check if user can create epics
   */
  canCreateEpic(projectRole?: string): boolean {
    return this.canPerformAction('CREATE_EPIC', projectRole);
  }

  /**
   * Check if user can create milestones
   */
  canCreateMilestone(projectRole?: string): boolean {
    return this.canPerformAction('CREATE_MILESTONE', projectRole);
  }

  /**
   * Check if user can assign tasks
   */
  canAssignTasks(projectRole?: string): boolean {
    return this.canPerformAction('ASSIGN_TASK', projectRole);
  }

  /**
   * Check if user can approve PRs
   */
  canApprovePR(projectRole?: string): boolean {
    return this.canPerformAction('APPROVE_PR', projectRole);
  }

  /**
   * Check if user can approve architecture decisions
   */
  canApproveArchitecture(projectRole?: string): boolean {
    return this.canPerformAction('APPROVE_ARCHITECTURE', projectRole);
  }

  /**
   * Check if user can request approval
   */
  canRequestApproval(projectRole?: string): boolean {
    return this.canPerformAction('REQUEST_APPROVAL', projectRole);
  }

  /**
   * Check if user can approve approvals
   */
  canApproveApprovals(projectRole?: string): boolean {
    return this.canPerformAction('APPROVE_APPROVALS', projectRole);
  }

  /**
   * Check if user can view approvals queue
   */
  canViewApprovals(projectRole?: string): boolean {
    return this.canPerformAction('VIEW_APPROVALS', projectRole);
  }

  /**
   * Check if user can view decisions
   */
  canViewDecisions(projectRole?: string): boolean {
    return this.canPerformAction('VIEW_DECISIONS', projectRole);
  }

  /**
   * Check if role is at least minimum required
   */
  isAtLeast(userRole: string, minimumRole: string): boolean {
    const userLevel = this.roleHierarchy[userRole] || 0;
    const minLevel = this.roleHierarchy[minimumRole] || 0;
    return userLevel >= minLevel;
  }

  /**
   * Get role display name
   */
  getRoleDisplayName(role: string): string {
    const displayNames: { [key: string]: string } = {
      'SDE_1': 'SDE-1',
      'SDE_2': 'SDE-2',
      'SENIOR': 'Senior Engineer',
      'STAFF': 'Staff Engineer',
      'PRINCIPAL': 'Principal Engineer',
      'ARCHITECT': 'Software Architect'
    };
    return displayNames[role] || role;
  }
}
