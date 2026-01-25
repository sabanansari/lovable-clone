package com.ansari.projects.lovable_clone.services.impl;

import com.ansari.projects.lovable_clone.dto.member.InviteMemberRequest;
import com.ansari.projects.lovable_clone.dto.member.MemberResponse;
import com.ansari.projects.lovable_clone.dto.member.UpdateMemberRoleRequest;
import com.ansari.projects.lovable_clone.entities.Project;
import com.ansari.projects.lovable_clone.entities.ProjectMember;
import com.ansari.projects.lovable_clone.entities.ProjectMemberId;
import com.ansari.projects.lovable_clone.entities.User;
import com.ansari.projects.lovable_clone.mapper.ProjectMemberMapper;
import com.ansari.projects.lovable_clone.repository.ProjectMemberRepository;
import com.ansari.projects.lovable_clone.repository.ProjectRepository;
import com.ansari.projects.lovable_clone.repository.UserRepository;
import com.ansari.projects.lovable_clone.services.ProjectMemberService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class ProjectMemberServiceImpl implements ProjectMemberService {

    ProjectMemberRepository projectMemberRepository;
    ProjectRepository projectRepository;
    ProjectMemberMapper projectMemberMapper;
    UserRepository userRepository;


    @Override
    public List<MemberResponse> getProjectMembers(Long projectId, Long userId) {
        Project project = getAccessibleProjectById(projectId, userId);

        return projectMemberRepository.findByIdProjectId(projectId)
                .stream()
                .map(projectMemberMapper::toProjectMemberResponseFromMember)
                .toList();

    }

    @Override
    public MemberResponse inviteMember(Long projectId, InviteMemberRequest request, Long userId) {
        Project project = getAccessibleProjectById(projectId, userId);

        User invitee = userRepository.findByUsername(request.username()).orElseThrow();

        if(invitee.getId().equals(userId)){
            throw new RuntimeException("You cannot invite yourself");
        }

        ProjectMemberId projectMemberId = new ProjectMemberId(projectId, invitee.getId());

        if(projectMemberRepository.existsById(projectMemberId)){
            throw new RuntimeException("You have already invited this user");
        }

        ProjectMember member = ProjectMember.builder()
                .id(projectMemberId)
                .project(project)
                .user(invitee)
                .projectRole(request.role())
                .invitedAt(Instant.now())
                .build();

        projectMemberRepository.save(member);


        return projectMemberMapper.toProjectMemberResponseFromMember(member);
    }

    @Override
    public MemberResponse updateMemberRole(Long projectId, Long memberId, UpdateMemberRoleRequest request, Long userId) {
        Project project = getAccessibleProjectById(projectId, userId);

        ProjectMemberId projectMemberId = new ProjectMemberId(projectId, memberId);

        ProjectMember member = projectMemberRepository.findById(projectMemberId).orElseThrow();
        member.setProjectRole(request.role());
        projectMemberRepository.save(member);


        return projectMemberMapper.toProjectMemberResponseFromMember(member);
    }

    @Override
    public MemberResponse removeProjectMember(Long projectId, Long memberId, Long userId) {
        Project project = getAccessibleProjectById(projectId, userId);

        ProjectMemberId projectMemberId = new ProjectMemberId(projectId, memberId);
        if(!projectMemberRepository.existsById(projectMemberId)){
            throw new RuntimeException("You have not invited this user");
        }
        projectMemberRepository.deleteById(projectMemberId);
        return null;
    }

    ///  INTERNAl FUNCTIONS
    private Project getAccessibleProjectById(Long id, Long userId){
        return projectRepository.findAccessibleProjectById(id, userId).orElseThrow();
    }


}
