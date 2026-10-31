package com.hrgenius.controller;
import com.hrgenius.entity.Department; import com.hrgenius.repository.DepartmentRepository;
import org.springframework.http.*; import org.springframework.security.access.prepost.PreAuthorize; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/departments")
public class DepartmentController {
 private final DepartmentRepository repo; public DepartmentController(DepartmentRepository repo){this.repo=repo;}
 @PostMapping @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER')") public ResponseEntity<Department> create(@RequestBody Department d){d.setId(null);return ResponseEntity.status(201).body(repo.save(d));}
 @GetMapping public java.util.List<Department> list(){return repo.findAll();}
 @PutMapping("/{id}") @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER')") public Department update(@PathVariable long id,@RequestBody Department d){Department x=repo.findById(id).orElseThrow();x.setName(d.getName());x.setDescription(d.getDescription());return repo.save(x);}
 @DeleteMapping("/{id}") @PreAuthorize("hasRole('ADMIN')") public ResponseEntity<?> delete(@PathVariable long id){repo.deleteById(id);return ResponseEntity.noContent().build();}
}
