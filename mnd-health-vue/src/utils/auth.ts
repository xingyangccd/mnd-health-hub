/**
 * 权限校验工具函数
 * @param permissions 需要的权限列表
 * @returns 是否拥有权限
 */
export const isAuth = (permissions: string[]): boolean => {
    if (!permissions || permissions.length === 0) {
        return false;
    }

    try {
        const permissionsJson = localStorage.getItem('permissions');
        if (!permissionsJson) return false;

        const localPermissions = JSON.parse(permissionsJson);
        if (!Array.isArray(localPermissions)) return false;

        return permissions.some(need => localPermissions.includes(need));
    } catch {
        return false;
    }
};