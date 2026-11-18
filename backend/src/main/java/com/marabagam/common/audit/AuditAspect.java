package com.marabagam.common.audit;

import com.marabagam.auth.dto.LoginRequestDto;
import com.marabagam.auth.dto.RegisterRequestDto;
import com.marabagam.user.domain.User;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AuditAspect {

    private static final Logger auditLog = LoggerFactory.getLogger("SECURITY_AUDIT");

    @Pointcut("execution(* com.marabagam.auth.service.AuthService.login(..))")
    public void loginPointcut() {}

    @Pointcut("execution(* com.marabagam.auth.service.AuthService.register(..))")
    public void registerPointcut() {}

    @Pointcut("execution(* com.marabagam.auth.service.AuthService.logout(..))")
    public void logoutPointcut() {}

    @Pointcut("execution(* com.marabagam.user.service.UserService.changePassword(..))")
    public void changePasswordPointcut() {}

    @AfterReturning("loginPointcut()")
    public void auditLoginSuccess(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args.length > 0 && args[0] instanceof LoginRequestDto dto) {
            auditLog.info("EVENT=LOGIN_SUCCESS USER_IDENTIFIER={}", dto.getUsernameOrEmail());
        }
    }

    @AfterThrowing(pointcut = "loginPointcut()", throwing = "ex")
    public void auditLoginFailure(JoinPoint joinPoint, Throwable ex) {
        Object[] args = joinPoint.getArgs();
        if (args.length > 0 && args[0] instanceof LoginRequestDto dto) {
            auditLog.warn("EVENT=LOGIN_FAILURE USER_IDENTIFIER={} REASON={}", dto.getUsernameOrEmail(), ex.getMessage());
        }
    }

    @AfterReturning("registerPointcut()")
    public void auditRegisterSuccess(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args.length > 0 && args[0] instanceof RegisterRequestDto dto) {
            auditLog.info("EVENT=USER_REGISTERED EMAIL={} USERNAME={}", dto.getEmail(), dto.getUsername());
        }
    }

    @AfterReturning("logoutPointcut()")
    public void auditLogout(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args.length > 1 && args[1] instanceof User currentUser) {
            auditLog.info("EVENT=LOGOUT USER_ID={}", currentUser.getId());
        } else {
            auditLog.info("EVENT=LOGOUT USER_ID=ANONYMOUS");
        }
    }

    @AfterReturning("changePasswordPointcut()")
    public void auditPasswordChange(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args.length > 0 && args[0] instanceof User currentUser) {
            auditLog.info("EVENT=PASSWORD_CHANGED USER_ID={}", currentUser.getId());
        }
    }

    @Pointcut("execution(* com.marabagam.learning.service.CourseService.createCourse(..))")
    public void createCoursePointcut() {}

    @Pointcut("execution(* com.marabagam.learning.service.CourseService.updateCourseStatus(..))")
    public void updateCourseStatusPointcut() {}

    @Pointcut("execution(* com.marabagam.learning.service.EnrollmentService.enrollCourse(..))")
    public void enrollCoursePointcut() {}

    @Pointcut("execution(* com.marabagam.learning.service.EnrollmentService.cancelEnrollment(..))")
    public void cancelEnrollmentPointcut() {}

    @AfterReturning("createCoursePointcut()")
    public void auditCreateCourse(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args.length > 1 && args[1] instanceof User currentUser) {
            auditLog.info("EVENT=COURSE_CREATED BY_USER_ID={}", currentUser.getId());
        }
    }

    @AfterReturning("updateCourseStatusPointcut()")
    public void auditCourseStatusChange(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args.length > 0) {
            auditLog.info("EVENT=COURSE_STATUS_CHANGED COURSE_ID={}", args[0]);
        }
    }

    @AfterReturning("enrollCoursePointcut()")
    public void auditEnrollCourse(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args.length > 1 && args[1] instanceof User currentUser) {
            auditLog.info("EVENT=COURSE_ENROLLED COURSE_ID={} USER_ID={}", args[0], currentUser.getId());
        }
    }

    @AfterReturning("cancelEnrollmentPointcut()")
    public void auditCancelEnrollment(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args.length > 1 && args[1] instanceof User currentUser) {
            auditLog.info("EVENT=ENROLLMENT_CANCELLED COURSE_ID={} USER_ID={}", args[0], currentUser.getId());
        }
    }

    @Pointcut("execution(* com.marabagam.art.service.HeritageLocationService.createLocation(..))")
    public void createLocationPointcut() {}

    @Pointcut("execution(* com.marabagam.art.service.HeritageLocationService.updateStatus(..))")
    public void updateLocationStatusPointcut() {}

    @Pointcut("execution(* com.marabagam.art.service.HeritageLocationService.archiveLocation(..))")
    public void archiveLocationPointcut() {}

    @Pointcut("execution(* com.marabagam.art.service.HeritageLocationService.deleteLocation(..))")
    public void deleteLocationPointcut() {}

    @AfterReturning("createLocationPointcut()")
    public void auditCreateLocation(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args.length > 1 && args[1] instanceof User currentUser) {
            auditLog.info("EVENT=HERITAGE_LOCATION_PROPOSED CREATED_BY_USER_ID={}", currentUser.getId());
        }
    }

    @AfterReturning("updateLocationStatusPointcut()")
    public void auditUpdateLocationStatus(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args.length > 1 && args[2] instanceof User currentUser) {
            auditLog.info("EVENT=HERITAGE_LOCATION_STATUS_CHANGED LOCATION_ID={} NEW_STATUS={} BY_USER_ID={}", args[0], args[1], currentUser.getId());
        }
    }

    @AfterReturning("archiveLocationPointcut()")
    public void auditArchiveLocation(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args.length > 1 && args[1] instanceof User currentUser) {
            auditLog.info("EVENT=HERITAGE_LOCATION_ARCHIVED LOCATION_ID={} BY_USER_ID={}", args[0], currentUser.getId());
        }
    }

    @AfterReturning("deleteLocationPointcut()")
    public void auditDeleteLocation(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args.length > 0) {
            auditLog.info("EVENT=HERITAGE_LOCATION_DELETED LOCATION_ID={}", args[0]);
        }
    }

    @Pointcut("execution(* com.marabagam.movement.service.MovementService.startPracticeSession(..))")
    public void startPracticeSessionPointcut() {}

    @Pointcut("execution(* com.marabagam.movement.service.MovementService.evaluatePracticeSession(..))")
    public void evaluatePracticeSessionPointcut() {}

    @Pointcut("execution(* com.marabagam.movement.service.MovementService.deletePracticeSession(..))")
    public void deletePracticeSessionPointcut() {}

    @Pointcut("execution(* com.marabagam.movement.service.MovementService.submitReferenceMovement(..))")
    public void submitReferencePointcut() {}

    @Pointcut("execution(* com.marabagam.movement.service.MovementService.approveReferenceMovement(..))")
    public void approveReferencePointcut() {}

    @AfterReturning("startPracticeSessionPointcut()")
    public void auditStartPracticeSession(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args.length > 1) {
            auditLog.info("EVENT=PRACTICE_SESSION_CREATED MOVEMENT_ID={} USER_ID={}", args[0], args[1]);
        }
    }

    @AfterReturning("evaluatePracticeSessionPointcut()")
    public void auditEvaluatePracticeSession(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args.length > 2) {
            auditLog.info("EVENT=PRACTICE_SESSION_EVALUATED SESSION_ID={} USER_ID={}", args[0], args[2]);
        }
    }

    @AfterReturning("deletePracticeSessionPointcut()")
    public void auditDeletePracticeSession(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args.length > 1) {
            auditLog.info("EVENT=PRACTICE_SESSION_DELETED SESSION_ID={} USER_ID={}", args[0], args[1]);
        }
    }

    @AfterReturning("submitReferencePointcut()")
    public void auditSubmitReference(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args.length > 2) {
            auditLog.info("EVENT=REFERENCE_SUBMITTED MOVEMENT_ID={} USER_ID={}", args[0], args[2]);
        }
    }

    @AfterReturning("approveReferencePointcut()")
    public void auditApproveReference(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args.length > 1) {
            auditLog.info("EVENT=REFERENCE_APPROVED REFERENCE_ID={} APPROVED_BY={}", args[0], args[1]);
        }
    }
}
