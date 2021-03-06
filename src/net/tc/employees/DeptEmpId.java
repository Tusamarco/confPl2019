package net.tc.employees;
// Generated 28-Apr-2019 3:52:07 PM by Hibernate Tools 5.4.2.Final



/**
 * DeptEmpId generated by hbm2java
 */
public class DeptEmpId  implements java.io.Serializable {


     private int empNo;
     private String deptNo;

    public DeptEmpId() {
    }

    public DeptEmpId(int empNo, String deptNo) {
       this.empNo = empNo;
       this.deptNo = deptNo;
    }
   
    public int getEmpNo() {
        return this.empNo;
    }
    
    public void setEmpNo(int empNo) {
        this.empNo = empNo;
    }
    public String getDeptNo() {
        return this.deptNo;
    }
    
    public void setDeptNo(String deptNo) {
        this.deptNo = deptNo;
    }


   public boolean equals(Object other) {
         if ( (this == other ) ) return true;
		 if ( (other == null ) ) return false;
		 if ( !(other instanceof DeptEmpId) ) return false;
		 DeptEmpId castOther = ( DeptEmpId ) other; 
         
		 return (this.getEmpNo()==castOther.getEmpNo())
 && ( (this.getDeptNo()==castOther.getDeptNo()) || ( this.getDeptNo()!=null && castOther.getDeptNo()!=null && this.getDeptNo().equals(castOther.getDeptNo()) ) );
   }
   
   public int hashCode() {
         int result = 17;
         
         result = 37 * result + this.getEmpNo();
         result = 37 * result + ( getDeptNo() == null ? 0 : this.getDeptNo().hashCode() );
         return result;
   }   


}


